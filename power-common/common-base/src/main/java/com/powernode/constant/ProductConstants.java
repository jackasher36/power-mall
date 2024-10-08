package com.powernode.constant;

/**
 * 产品业务模块常量类
 */
public interface ProductConstants {

    /**
     * 商品所有类目数据存放到redis中的KEY
     */
    String ALL_CATEGORY_LIST_KEY = "'allCategory'";

    /**
     * 商品一级类目数据存放到redis中的KEY
     */
    String FIRST_CATEGORY_LIST_KEY = "'firstCategory'";

    /**
     * 状态正常的商品分组标签数据存放到redis中的KEY
     */
    String PROD_TAG_NORMAL_KEY = "'prodTagNormal'";

    /**
     * 商品属性数据存放到redis中的KEY
     */
    String PROD_PROP_KEY = "'prodProp'";

    /**
     * 小程序：分组标签数据存放到redis中的KEY
     */
    String WX_PROD_TAG = "'wxProdTag'";

    /**
     * 小程序：商品一级类目数据存放到redis中的KEY
     */
    String WX_FIRST_CATEGORY = "'wxFirstCategory'";
}
