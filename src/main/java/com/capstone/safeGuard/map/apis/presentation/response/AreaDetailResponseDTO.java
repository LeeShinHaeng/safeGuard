package com.capstone.safeGuard.map.apis.presentation.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaDetailResponseDTO {
    private String isLiving;
    public Double XOfPointA;
    public Double YOfPointA;
    public Double XOfPointB;
    public Double YOfPointB;
    public Double XOfPointC;
    public Double YOfPointC;
    public Double XOfPointD;
    public Double YOfPointD;

    @Builder
    public AreaDetailResponseDTO(String isLiving, Double XOfPointA, Double YOfPointA, Double XOfPointB, Double YOfPointB, Double XOfPointC, Double YOfPointC, Double XOfPointD, Double YOfPointD) {
        this.isLiving = isLiving;
        this.XOfPointA = XOfPointA;
        this.YOfPointA = YOfPointA;
        this.XOfPointB = XOfPointB;
        this.YOfPointB = YOfPointB;
        this.XOfPointC = XOfPointC;
        this.YOfPointC = YOfPointC;
        this.XOfPointD = XOfPointD;
        this.YOfPointD = YOfPointD;
    }
}
