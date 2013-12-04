/*
 * ISelectorCreator.java Version 1.0 May 6, 2013
 * *
 * Copyright (c) 2010 by Tmax Soft co., Ltd.
 * All rights reserved.
 * *
 * This software is the confidential and proprietary information of
 * Tmax Soft co.,Ltd("Confidential Information").
 * You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement
 * entered into with Tmax Soft co., Ltd.
 */
package com.tmax.nio.api;


import java.util.concurrent.ExecutorService;


/**
 * The Interface ISelectorCreator.
 */
public interface ISelectorCreator {
    /**
     * New selector.
     * @param id the id
     * @param service the service
     * @param config the config
     * @return the i selector
     */
    ISelector newSelector(String id, ExecutorService service, ISelectorConfig config);
}
