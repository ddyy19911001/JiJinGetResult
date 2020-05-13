package com.dy.getresultapp;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

@Entity
public class JiJingInfo {
    @Id(autoincrement = true)//设置自增长
    private Long id;
    private String name;
    @Index(unique = true)
    private String code;

   

    @Generated(hash = 1419339414)
    public JiJingInfo() {
    }

    @Generated(hash = 618808204)
    public JiJingInfo(Long id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
