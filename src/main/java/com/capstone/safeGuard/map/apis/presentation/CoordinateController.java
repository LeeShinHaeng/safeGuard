package com.capstone.safeGuard.map.apis.presentation;

import com.capstone.safeGuard.map.domain.domain.Coordinate;
import com.capstone.safeGuard.map.apis.presentation.request.coordinate.AddAreaDTO;
import com.capstone.safeGuard.map.apis.presentation.request.coordinate.AreaDetailRequestDTO;
import com.capstone.safeGuard.map.apis.presentation.request.coordinate.DeleteAreaDTO;
import com.capstone.safeGuard.map.apis.presentation.request.coordinate.GetChildNameDTO;
import com.capstone.safeGuard.map.apis.presentation.response.AreaDetailResponseDTO;
import com.capstone.safeGuard.map.apis.presentation.response.ReadAreaResponseDTO;
import com.capstone.safeGuard.map.apis.application.CoordinateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CoordinateController {
    private final CoordinateService coordinateService;

    @PostMapping("/add-safe")
    public ResponseEntity<Map<String, String>> addLivingArea(@RequestBody AddAreaDTO dto){
        HashMap<String, String> result = new HashMap<>();

        log.info("add-safe 받았음");
        Long areaId = coordinateService.addLivingArea(dto);
        if(areaId == 0L){
            log.info("add-safe 실패");
            return addErrorStatus(result);
        }

        log.info("add-safe 성공");
        result.put("areaId", areaId.toString());
        return addOkStatus(result);
    }


    @PostMapping("/add-dangerous")
    public ResponseEntity<Map<String, String>> addForbiddenArea(@RequestBody AddAreaDTO dto){
        HashMap<String, String> result = new HashMap<>();

        log.info("add-dangerous 받았음");
        Long areaId = coordinateService.addForbiddenArea(dto);
        if(areaId == 0L){
            log.info("add-dangerous 실패");
            return addErrorStatus(result);
        }

        log.info("add-dangerous 성공");
        result.put("areaId", areaId.toString());
        return addOkStatus(result);
    }

    @PostMapping("/delete-area")
    public ResponseEntity<Map<String, String>> deleteArea(@RequestBody DeleteAreaDTO dto){
        HashMap<String, String> result = new HashMap<>();

        if(! coordinateService.deleteArea(dto)){
            return addErrorStatus(result);
        }

        return addOkStatus(result);
    }

    @PostMapping("/read-areas")
    public ResponseEntity<Map<String, ReadAreaResponseDTO>> readAreas(@RequestBody GetChildNameDTO dto){
        HashMap<String, ReadAreaResponseDTO> result = new HashMap<>();

        // 1. child에 저장되어 있는 coordinate 불러오기
        ArrayList<Coordinate> coordinates = coordinateService.readAreasByChild(dto.getChildName());

        // 2. responseDTO로 변경
        for (Coordinate coordinate : coordinates) {
            result.put(coordinate.getCoordinateId()+"",
                    ReadAreaResponseDTO.builder()
                            .isLiving(coordinate.isLivingArea() + "")
                            .XOfPointA(coordinate.getXOfNorthEast())
                            .YOfPointA(coordinate.getYOfNorthEast())
                            .XOfPointB(coordinate.getXOfNorthWest())
                            .YOfPointB(coordinate.getYOfNorthWest())
                            .XOfPointC(coordinate.getXOfSouthWest())
                            .YOfPointC(coordinate.getYOfSouthWest())
                            .XOfPointD(coordinate.getXOfSouthEast())
                            .YOfPointD(coordinate.getYOfSouthEast())
                            .build()
            );
        }

        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/area-detail")
    public ResponseEntity<Map<String, AreaDetailResponseDTO>> areaDetail(@RequestBody AreaDetailRequestDTO dto){
        Map<String, AreaDetailResponseDTO> result = new HashMap<>();

        Coordinate coordinate = coordinateService.findAreaById(dto.getAreaId());
        if(coordinate == null){
            return ResponseEntity.status(400).build();
        }

        result.put(coordinate.getCoordinateId()+"",
                AreaDetailResponseDTO.builder()
                        .isLiving(coordinate.isLivingArea() + "")
                        .XOfPointA(coordinate.getXOfNorthEast())
                        .YOfPointA(coordinate.getYOfNorthEast())
                        .XOfPointB(coordinate.getXOfNorthWest())
                        .YOfPointB(coordinate.getYOfNorthWest())
                        .XOfPointC(coordinate.getXOfSouthWest())
                        .YOfPointC(coordinate.getYOfSouthWest())
                        .XOfPointD(coordinate.getXOfSouthEast())
                        .YOfPointD(coordinate.getYOfSouthEast())
                        .build()
        );

        return ResponseEntity.ok().body(result);
    }



    private static ResponseEntity<Map<String, String>> addOkStatus(Map<String, String> result) {
        result.put("status", "200");
        return ResponseEntity.ok().body(result);
    }

    private static ResponseEntity<Map<String, String>> addErrorStatus(Map<String, String> result) {
        result.put("status", "400");
        return ResponseEntity.status(400).body(result);
    }
}
