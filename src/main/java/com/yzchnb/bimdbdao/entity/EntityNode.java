package com.yzchnb.bimdbdao.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Document(collection = "EntityNode")
@CompoundIndex(def = "{'name' = 1, 'links.node' = 1}")
@CompoundIndex(def = "{'uniqueId' = 1, 'links.uniqueId' = 1}")
public class EntityNode {

    @Id
    @JSONField(deserialize = false)
    private String _id;
    @Field
    @Indexed
    private String name;
    @Field
    @Indexed
    private int uniqueId;
    @Field
    private Set<NodeToRelation> links;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<NodeToRelation> getLinks() {
        return links;
    }

    public void setLinks(List<String> nodes, List<String> relations, List<Integer> directions) {
        assert nodes.size() == relations.size();
        this.links = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            links.add(new NodeToRelation(nodes.get(i), relations.get(i), directions.get(i)));
        }
    }

    public void setLinks(Set<NodeToRelation> links){
        this.links = links;
    }

    public void addLinks(Set<NodeToRelation> set){
        if(this.links == null){
            this.links = new HashSet<>();
        }
        this.links.addAll(set);
    }

    public void addLink(String node, String relation, int direction){
        if(this.links == null){
            this.links = new HashSet<>();
        }
        this.links.add(new NodeToRelation(node, relation, direction));
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityNode that = (EntityNode) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, links);
    }
}
