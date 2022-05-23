package com.dingtalk.h5app.quickstart.controller;

import com.dingtalk.h5app.quickstart.BaseSpringBootTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description unit test
 * @Author: spp
 * @Date: 2022/2/10
 * @Version 1.0
 */
public class ContactControllerTest extends BaseSpringBootTest {

	@Autowired
	private ContactController contactController;

	@Test
	public void test(){
		contactController.listDepartment("1");
	}

}