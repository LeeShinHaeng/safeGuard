package com.capstone.safeGuard.apis.map.presentation.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReadAreaResponseDTO {
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
    public ReadAreaResponseDTO(String isLiving, Double XOfPointA, Double YOfPointA, Double XOfPointB, Double YOfPointB, Double XOfPointC, Double YOfPointC, Double XOfPointD, Double YOfPointD) {
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
