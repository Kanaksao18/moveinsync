package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
public class AdminUser extends BaseEntity {

    private String username;

    private String password;

    private String role; // ADMIN / VIEWER
}