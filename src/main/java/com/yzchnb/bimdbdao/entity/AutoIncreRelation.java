package com.yzchnb.bimdbdao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "AutoIncreRelation")
public class AutoIncreRelation {
    @Id
    private String _id;
    @Field
    private int currCount;
}
