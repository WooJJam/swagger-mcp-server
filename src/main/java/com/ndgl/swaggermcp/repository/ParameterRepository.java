package com.ndgl.swaggermcp.repository;

import com.ndgl.swaggermcp.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {
}
