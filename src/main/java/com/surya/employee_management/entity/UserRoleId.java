package com.surya.employee_management.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class UserRoleId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long roleId;
}
