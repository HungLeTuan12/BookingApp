package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserModel {
    private Long id;
    private String avatar;
    private String fullName;
    private String userName;
    private String phone;
    private String email;
    private String description;
    private String trangthai;
    private Long roleId;
    private boolean enabled;
    private MajorModel major;
    private Integer totalPages;
    private BigDecimal fee;

    public UserModel(Long id, String avatar, String fullName, String userName, String phone, String email, String description, Long roleId, boolean enabled, MajorModel major, String tt, BigDecimal fee) {
        this.id = id;
        this.avatar = avatar;
        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
        this.description = description;
        this.phone = phone;
        this.roleId = roleId;
        this.enabled = enabled;
        this.major = major;
        this.trangthai = tt;
        this.fee = fee;
    }
}
