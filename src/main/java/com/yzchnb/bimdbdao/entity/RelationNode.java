package com.yzchnb.bimdbdao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "RelationNode")
public class RelationNode {
    @Id
    private String _id;
    @Field
    @Indexed
    private String name;
    @Field
    @Indexed
    private int uniqueId;
}
