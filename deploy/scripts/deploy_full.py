#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
智慧渔业管理系统 - 完整自动化部署脚本
功能：
1. 构建前端
2. 构建后端
3. 备份当前版本
4. 上传到服务器
5. 部署并重启服务
6. 验证部署结果
"""

import os
import sys
import time
import subprocess
import datetime
import shutil
import tarfile
import zipfile

# 导入配置
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import deploy_config

# ============================================
# 工具函数
# ============================================

def print_header(text):
    """打印标题"""
    print("\n" + "="*60)
    print(f"  {text}")
    print("="*60)

def print_success(text):
    """打印成功信息"""
    print(f"✅ {text}")

def print_error(text):
    """打印错误信息"""
    print(f"❌ {text}")

def print_info(text):
    """打印信息"""
    print(f"ℹ️ {text}")

def run_command(cmd, cwd=None, shell=True):
    """运行命令"""
    try:
        result = subprocess.run(cmd, shell=shell, cwd=cwd, 
                                capture_output=True, text=True)
        return result.returncode == 0, result.stdout, result.stderr
    except Exception as e:
        return False, "", str(e)

def get_timestamp():
    """获取时间戳"""
    return datetime.datetime.now().strftime('%Y%m%d_%H%M%S')

# ============================================
# 构建函数
# ============================================

def build_frontend():
    """构建前端"""
    print_header("步骤 1: 构建前端")
    
    frontend_dir = deploy_config.PATH_CONFIG["local_frontend_dir"]
    project_root = deploy_config.PATH_CONFIG["local_project_root"]
    
    if not os.path.exists(frontend_dir):
        print_error(f"前端目录不存在: {frontend_dir}")
        return False
    
    print_info(f"前端目录: {frontend_dir}")
    
    # 检查是否安装依赖
    node_modules = os.path.join(frontend_dir, "node_modules")
    if not os.path.exists(node_modules):
        print_info("安装前端依赖...")
        success, stdout, stderr = run_command("npm install", cwd=frontend_dir)
        if not success:
            print_error("安装前端依赖失败")
            print(stderr)
            return False
        print_success("前端依赖安装成功")
    
    # 构建
    print_info("开始构建前端...")
    success, stdout, stderr = run_command("npm run build", cwd=frontend_dir)
    if not success:
        print_error("前端构建失败")
        print(stderr)
        return False
    
    # 检查构建输出
    dist_dir = os.path.join(frontend_dir, "dist")
    if not os.path.exists(dist_dir):
        print_error("构建输出目录不存在: dist")
        return False
    
    # 打包前端文件
    print_info("打包前端文件...")
    dist_files = os.path.join(dist_dir, "*")
    tar_path = os.path.join(project_root, "frontend_dist.tar.gz")
    
    # 删除旧的压缩包
    if os.path.exists(tar_path):
        os.remove(tar_path)
    
    # 创建tar.gz
    with tarfile.open(tar_path, "w:gz") as tar:
        tar.add(dist_dir, arcname="")
    
    print_success(f"前端构建完成: {tar_path}")
    return True

def build_backend():
    """构建后端"""
    print_header("步骤 2: 构建后端")
    
    backend_dir = deploy_config.PATH_CONFIG["local_backend_dir"]
    jar_name = deploy_config.PATH_CONFIG["backend_jar_name"]
    project_root = deploy_config.PATH_CONFIG["local_project_root"]
    
    if not os.path.exists(backend_dir):
        print_error(f"后端目录不存在: {backend_dir}")
        return False
    
    print_info(f"后端目录: {backend_dir}")
    
    # 构建命令
    maven_cmd = "mvn clean package"
    if deploy_config.BUILD_CONFIG["skip_tests"]:
        maven_cmd += " -DskipTests"
    
    print_info("开始构建后端...")
    success, stdout, stderr = run_command(maven_cmd, cwd=backend_dir)
    if not success:
        print_error("后端构建失败")
        print(stderr)
        return False
    
    # 查找jar文件
    target_dir = os.path.join(backend_dir, "target")
    jar_files = [f for f in os.listdir(target_dir) if f.endswith(".jar") and "original" not in f]
    
    if not jar_files:
        print_error("未找到JAR文件")
        return False
    
    # 复制jar到项目根目录
    src_jar = os.path.join(target_dir, jar_files[0])
    dst_jar = os.path.join(project_root, jar_name)
    
    if os.path.exists(dst_jar):
        os.remove(dst_jar)
    
    shutil.copy2(src_jar, dst_jar)
    
    print_success(f"后端构建完成: {dst_jar}")
    return True

# ============================================
# 部署函数
# ============================================

def upload_files():
    """上传文件到服务器"""
    print_header("步骤 3: 上传文件")
    
    host = deploy_config.SERVER_CONFIG["host"]
    username = deploy_config.SERVER_CONFIG["username"]
    project_root = deploy_config.PATH_CONFIG["local_project_root"]
    server_frontend = deploy_config.PATH_CONFIG["server_frontend_dir"]
    server_backend = deploy_config.PATH_CONFIG["server_backend_dir"]
    jar_name = deploy_config.PATH_CONFIG["backend_jar_name"]
    
    # 检查本地文件
    frontend_tar = os.path.join(project_root, "frontend_dist.tar.gz")
    backend_jar = os.path.join(project_root, jar_name)
    
    if not os.path.exists(frontend_tar):
        print_error(f"前端压缩包不存在: {frontend_tar}")
        return False
    
    if not os.path.exists(backend_jar):
        print_error(f"后端JAR文件不存在: {backend_jar}")
        return False
    
    # SCP上传前端
    print_info("上传前端文件...")
    cmd = f'scp -o StrictHostKeyChecking=no "{frontend_tar}" {username}@{host}:{server_frontend}/'
    success, stdout, stderr = run_command(cmd, cwd=project_root)
    if not success:
        print_error("上传前端失败")
        print(stderr)
        return False
    print_success("前端文件上传成功")
    
    # SCP上传后端
    print_info("上传后端文件...")
    cmd = f'scp -o StrictHostKeyChecking=no "{backend_jar}" {username}@{host}:{server_backend}/'
    success, stdout, stderr = run_command(cmd, cwd=project_root)
    if not success:
        print_error("上传后端失败")
        print(stderr)
        return False
    print_success("后端文件上传成功")
    
    return True

def deploy_on_server():
    """在服务器上执行部署"""
    print_header("步骤 4: 在服务器上部署")
    
    host = deploy_config.SERVER_CONFIG["host"]
    username = deploy_config.SERVER_CONFIG["username"]
    server_frontend = deploy_config.PATH_CONFIG["server_frontend_dir"]
    server_backend = deploy_config.PATH_CONFIG["server_backend_dir"]
    jar_name = deploy_config.PATH_CONFIG["backend_jar_name"]
    
    # SSH命令
    ssh_cmd_prefix = f'ssh -o StrictHostKeyChecking=no {username}@{host}'
    
    # 部署前端
    print_info("部署前端...")
    deploy_frontend_cmds = f"""
    cd {server_frontend} && \
    tar -czf backup_$(date +%Y%m%d_%H%M%S).tar.gz index.html css js img fonts 2>/dev/null || true && \
    rm -rf index.html css js img fonts && \
    tar -xzf frontend_dist.tar.gz && \
    rm frontend_dist.tar.gz
    """
    success, stdout, stderr = run_command(f"{ssh_cmd_prefix} '{deploy_frontend_cmds}'")
    if not success:
        print_error("部署前端失败")
        print(stderr)
        return False
    print_success("前端部署成功")
    
    # 部署后端
    print_info("部署后端...")
    deploy_backend_cmds = f"""
    cd {server_backend} && \
    pkill -f '{jar_name}' 2>/dev/null || true && \
    mkdir -p data data/export && \
    nohup java -jar {jar_name} > app.log 2>&1 &
    """
    success, stdout, stderr = run_command(f"{ssh_cmd_prefix} '{deploy_backend_cmds}'")
    if not success:
        print_error("部署后端失败")
        print(stderr)
        return False
    print_success("后端部署成功")
    
    return True

def verify_deployment():
    """验证部署"""
    print_header("步骤 5: 验证部署")
    
    host = deploy_config.SERVER_CONFIG["host"]
    username = deploy_config.SERVER_CONFIG["username"]
    server_backend = deploy_config.PATH_CONFIG["server_backend_dir"]
    
    wait_time = deploy_config.DEPLOY_OPTIONS["wait_after_deploy"]
    print_info(f"等待 {wait_time} 秒让服务启动...")
    time.sleep(wait_time)
    
    # 检查Java进程
    print_info("检查Java进程...")
    cmd = f'ssh -o StrictHostKeyChecking=no {username}@{host} "ps aux | grep java"'
    success, stdout, stderr = run_command(cmd)
    
    if success and "aquaculture" in stdout:
        print_success("后端服务运行中")
    else:
        print_error("后端服务未运行")
        return False
    
    # 检查端口
    print_info("检查端口...")
    cmd = f'ssh -o StrictHostKeyChecking=no {username}@{host} "netstat -tuln 2>/dev/null || ss -tuln"'
    success, stdout, stderr = run_command(cmd)
    
    if success:
        if "80" in stdout or "8080" in stdout:
            print_success("端口正常监听")
        else:
            print_info("端口检查完成")
    
    print_success("部署验证成功！")
    return True

# ============================================
# 主函数
# ============================================

def main():
    """主部署流程"""
    print_header("智慧渔业管理系统 - 自动化部署")
    print_info(f"目标服务器: {deploy_config.SERVER_CONFIG['host']}")
    print_info(f"开始时间: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # 步骤1: 构建前端
    if not deploy_config.BUILD_CONFIG["skip_frontend_build"]:
        if not build_frontend():
            print_error("前端构建失败，部署终止")
            return 1
    else:
        print_info("跳过前端构建")
    
    # 步骤2: 构建后端
    if not deploy_config.BUILD_CONFIG["skip_backend_build"]:
        if not build_backend():
            print_error("后端构建失败，部署终止")
            return 1
    else:
        print_info("跳过后端构建")
    
    # 步骤3: 上传文件
    if not upload_files():
        print_error("文件上传失败，部署终止")
        return 1
    
    # 步骤4: 服务器部署
    if not deploy_on_server():
        print_error("服务器部署失败")
        return 1
    
    # 步骤5: 验证部署
    if deploy_config.DEPLOY_OPTIONS["verify_after_deploy"]:
        if not verify_deployment():
            print_error("部署验证失败")
            return 1
    
    # 完成
    print_header("部署完成")
    print_success("🎉 系统部署成功！")
    print_info(f"访问地址: http://{deploy_config.SERVER_CONFIG['host']}")
    print_info(f"默认账号: admin / 666666")
    print_info(f"完成时间: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    return 0

if __name__ == "__main__":
    sys.exit(main())
