package com.yzchnb.bimdbdao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Document(collection = "EntityNode")
@CompoundIndex(
    name = "nameToLinks", def = "{'name' = 1, 'links.node' = 1}"
)
public class EntityNode {

    @Id
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

    public void setLinks(List<String> nodes, List<String> relations) {
        assert nodes.size() == relations.size();
        this.links = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            links.add(new NodeToRelation(nodes.get(i), relations.get(i)));
        }
    }

    public void addLink(String node, String relation){
        if(this.links == null){
            this.links = new HashSet<>();
        }
        this.links.add(new NodeToRelation(node, relation));
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

}

class NodeToRelation{
    private String node;
    private String relation;

    public NodeToRelation(String node, String relation) {
        this.node = node;
        this.relation = relation;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeToRelation that = (NodeToRelation) o;
        return node.equals(that.node) &&
                relation.equals(that.relation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, relation);
    }
}
