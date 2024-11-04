package com.capstone.safeGuard.apis.map.application;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.AddAreaRequest;
import com.capstone.safeGuard.apis.map.presentation.request.coordinate.DeleteAreaRequest;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoordinateService {
    private final CoordinateRepository coordinateRepository;
    private final ChildRepository childRepository;

    @Transactional
    public Long addForbiddenArea(AddAreaRequest addAreaRequest) {
        Child foundChild = childRepository.findBychildName(addAreaRequest.getChildName());
        if (foundChild == null) {
            return 0L;
        }

        Coordinate coordinate = addAreaRequest.dtoToDomain(foundChild, false);
        // child와 coordinate에 저장
        foundChild.getForbiddenAreas().add(coordinate);
        coordinateRepository.save(coordinate);

        log.info("addForbiddenArea 성공 ");
        return coordinate.getCoordinateId();
    }

    @Transactional
    public Long addLivingArea(AddAreaRequest addAreaRequest) {
        log.info("addLivingArea 도착");
        Child foundChild = childRepository.findBychildName(addAreaRequest.getChildName());
        if (foundChild == null) {
            log.info("No Such Child");
            return 0L;
        }

        Coordinate coordinate = addAreaRequest.dtoToDomain(foundChild, true);

        // child와 coordinate에 저장
        log.info(addAreaRequest.getXOfPointA() + " = " + coordinate.getXOfSouthEast());
        foundChild.getLivingAreas().add(coordinate);
        coordinateRepository.save(coordinate);

        log.info("addLivingArea 성공 ");
        return coordinate.getCoordinateId();
    }

    @Transactional
    public boolean deleteArea(DeleteAreaRequest dto) {
        String areaID = dto.getAreaID();
        String childName = dto.getChildName();

        log.info("deleteArea 시작");
        Child foundChild = childRepository.findBychildName(childName);
        Optional<Coordinate> foundCoordinate = coordinateRepository.findById(Long.parseLong(areaID));
        if ((foundChild == null) ||
                foundCoordinate.isEmpty() ||
                (!foundChild.equals(foundCoordinate.get().getChild()))) {
            return false;
        }

        coordinateRepository.delete(foundCoordinate.get());
        return true;
    }

    public ArrayList<Coordinate> readAreasByChild(String childName) {
        Child foundChild = childRepository.findBychildName(childName);

        ArrayList<Coordinate> foundCoordinates = coordinateRepository.findAllByChild(foundChild);
        if (foundCoordinates.isEmpty()) {
            return null;
        }
        return foundCoordinates;
    }

    public Coordinate findAreaById(String areaId) {
        return coordinateRepository.findById(Long.parseLong(areaId)).get();
    }
}
