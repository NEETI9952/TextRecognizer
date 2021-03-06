package com.example.invoicetextdetector;

import java.util.List;

public class Pradrclass {

    private List<addressClass> addr;

    public Pradrclass(List<addressClass> addr) {
        this.addr = addr;
    }

    public Pradrclass() {

    }

    //    pradr":{"ntr":"Service Provision, Recipient of Goods or Services,
//    Others","

//    addr":{
//    "stcd":"Delhi",
//    "dst":"North West Delhi",
//    "bnm":"",
//    "city":"",
//    "pncd":"110021",
//    "lt":"",
//    "loc":"NIKETAN",
//    "bno":"HOUSE NO. 8",
//    "lg":"",
//     "flno":"GROUND FLOOR
//    "st":"SATYA"}

//  }


    public List<addressClass> getAddr() {
        return addr;
    }

    public void setAddr(List<addressClass> addr) {
        this.addr = addr;
    }
}

