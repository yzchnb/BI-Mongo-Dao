package com.yzchnb.bimdbdao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "AutoIncreEntity")
public class AutoIncreEntity {
    @Id
    private String _id;
    @Field
    private int currCount;

    public AutoIncreEntity(int currCount) {
        this.currCount = currCount;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int getCurrCount() {
        return currCount;
    }

    public void setCurrCount(int currCount) {
        this.currCount = currCount;
    }
}
