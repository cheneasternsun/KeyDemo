package com.dongchen.keydemo;

/**
 *
 */
public class JumpBean {
//    public static final int JUMP_TYPE = 1;
//    public static final int URL = 2;

    private int page;   //保存该例子在书中的页码数
    private String content; //item 显示的内容
    private int jumpType;   //跳转类型，包括：显示跳转、隐式跳转。
    private String absClsUrlOrAction; //类的绝对路径(即类的引用) 或 Action  如：com.dongcheng.qunyingzhuan.CustomViewBlockEventActivity
//    private String packageUrl;  //包路径   如：com.dongcheng.qunyingzhuan


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getAbsClsUrlOrAction() {
        return absClsUrlOrAction;
    }

    public void setAbsClsUrlOrAction(String absClsUrlOrAction) {
        this.absClsUrlOrAction = absClsUrlOrAction;
    }

    public int getJumpType() {
        return jumpType;
    }

    public void setJumpType(int jumpType) {
        this.jumpType = jumpType;
    }
}
