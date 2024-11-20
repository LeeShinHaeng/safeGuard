package com.capstone.safeGuard.apis.map.presentation.request.coordinate;

public record DeleteAreaRequest (
    String areaID,
    String childName,
    String memberID
){
}
