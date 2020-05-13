package com.dy.getresultapp;

public class HsInfos {


    /**
     * rc : 0
     * rt : 4
     * svr : 182993300
     * lt : 1
     * full : 1
     * data : {"f43":289156,"f44":289790,"f45":287123}
     */

    private int rc;
    private int rt;
    private long svr;
    private int lt;
    private int full;
    private DataBean data;

    @Override
    public String toString() {
        return "HsInfos{" +
                "rc=" + rc +
                ", rt=" + rt +
                ", svr=" + svr +
                ", lt=" + lt +
                ", full=" + full +
                ", data=" + data +
                '}';
    }

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public int getRt() {
        return rt;
    }

    public void setRt(int rt) {
        this.rt = rt;
    }

    public long getSvr() {
        return svr;
    }

    public void setSvr(long svr) {
        this.svr = svr;
    }

    public int getLt() {
        return lt;
    }

    public void setLt(int lt) {
        this.lt = lt;
    }

    public int getFull() {
        return full;
    }

    public void setFull(int full) {
        this.full = full;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * f43 : 289156
         * f44 : 289790
         * f45 : 287123
         */
        //当前
        private long f43;
        //最高
        private long f44;
        //最低
        private long f45;
        //起点价格
        private long f60;

        @Override
        public String toString() {
            return "DataBean{" +
                    "f43=" + f43 +
                    ", f44=" + f44 +
                    ", f45=" + f45 +
                    ", f60=" + f60 +
                    '}';
        }

        public long getF60() {
            return f60;
        }

        public void setF60(long f60) {
            this.f60 = f60;
        }

        public long getF43() {
            return f43;
        }

        public void setF43(long f43) {
            this.f43 = f43;
        }

        public long getF44() {
            return f44;
        }

        public void setF44(long f44) {
            this.f44 = f44;
        }

        public long getF45() {
            return f45;
        }

        public void setF45(long f45) {
            this.f45 = f45;
        }
    }
}
