package com.ndgl.swaggermcp.repository;

import com.ndgl.swaggermcp.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {

    /**
     * API Endpoint ID로 Parameter 목록 조회
     */
    List<Parameter> findByApiEndpointId(Long apiEndpointId);
}
