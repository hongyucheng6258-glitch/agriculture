package com.aquaculture.dto;

import com.aquaculture.entity.Trace;
import com.aquaculture.entity.Feeding;
import com.aquaculture.entity.Disease;
import com.aquaculture.entity.WaterQuality;
import lombok.Data;

import java.util.List;

@Data
public class TraceDetailDTO {
    private Trace trace;
    private List<Feeding> feedingList;
    private List<Disease> diseaseList;
    private List<WaterQuality> waterQualityList;
}
