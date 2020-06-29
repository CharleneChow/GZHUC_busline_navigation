package com.example.tabui;

public class Side {
    private int preNode; //前驱节点
    private int nextNode;//后继节点
    private double weight;//权重

    public Side(int preNode,int nextNode,double weight){
        this.preNode = preNode;
        this.nextNode = nextNode;
        this.weight = weight;
    }

    public int getPreNode(){
        return this.preNode;
    }

    public void setPreNode(int preNode){
        this.preNode = preNode;
    }

    public int getNextNode(){
        return this.nextNode;
    }

    public void setNextNode(int nextNode){
        this.nextNode = nextNode;
    }

    public double getWeight(){
        return this.weight;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }

}
