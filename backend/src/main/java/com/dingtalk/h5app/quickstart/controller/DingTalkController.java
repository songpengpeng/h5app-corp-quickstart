package com.dingtalk.h5app.quickstart.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiProcessInstanceTerminateRequest;
import com.dingtalk.api.request.OapiProcessinstanceCreateRequest;
import com.dingtalk.api.response.OapiProcessInstanceTerminateResponse;
import com.dingtalk.api.response.OapiProcessinstanceCreateResponse;
import com.dingtalk.h5app.quickstart.config.AppConfig;
import com.dingtalk.h5app.quickstart.domain.ServiceResult;
import com.dingtalk.h5app.quickstart.service.TokenService;
import com.dingtalk.h5app.quickstart.util.DingCallbackCrypto;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 接口类
 */
@RestController
@RequestMapping(value = "/dingtalk")
public class DingTalkController {

    private static final Logger _logger = LoggerFactory.getLogger(DingTalkController.class);

    // 事件订阅
    public static final String TOKEN = "q4a2VJMCzfMYvvr4p";
    public static final String AES_KEY = "oy4PoIjGQ7LsiHVEuAWLRvJxXgxPPDqCfBAD28yFo77";
    public static final String APP_KEY = "dinghvrwdre9ojjwp1lr";

    private TokenService tokenService;
    private AppConfig appConfig;

    @Autowired
    public DingTalkController(TokenService tokenService,
        AppConfig appConfig) {
        this.tokenService = tokenService;
        this.appConfig = appConfig;
    }

    /**
     * 事件订阅
     *
     * @param request
     * @param bodyJson
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/event", method = RequestMethod.POST)
    public Map<String, String> event(HttpServletRequest request, @RequestBody JSONObject bodyJson) {
        // 处理"/users/"的POST请求，用来创建User
        // 除了@ModelAttribute绑定参数之外，还可以通过@RequestParam从页面中传递参数
        // 1. 从http请求中获取加解密参数
        try {
            String msg_signature = request.getParameter("msg_signature");
            if (msg_signature == null) {
                msg_signature = request.getParameter("signature");
            }
            String timeStamp = request.getParameter("timeStamp");
            if (timeStamp == null) {
                timeStamp = request.getParameter("timestamp");
            }
            String nonce = request.getParameter("nonce");
            String encrypt = bodyJson.getString("encrypt");

            // 2. 使用加解密类型
            DingCallbackCrypto callbackCrypto = new DingCallbackCrypto(TOKEN, AES_KEY, APP_KEY);
            final String decryptMsg = callbackCrypto.getDecryptMsg(msg_signature, timeStamp, nonce, encrypt);

            // 3. 反序列化回调事件json数据
            JSONObject eventJson = JSON.parseObject(decryptMsg);
            _logger.info("eventJson={}", eventJson.toJSONString());
            String eventType = eventJson.getString("EventType");

            // 4. 根据EventType分类处理
            if ("check_url".equals(eventType)) {
                // 测试回调url的正确性
            } else if ("user_add_org".equals(eventType)) {
                // 处理通讯录用户增加时间
            } else {
                // 添加其他已注册的
            }

            // 5. 返回success的加密数据
            Map<String, String> successMap = callbackCrypto.getEncryptedMap("success");
            return successMap;
        } catch (DingCallbackCrypto.DingTalkEncryptException e) {
            e.printStackTrace();
            Map<String, String> resultMap = new HashMap<String, String>();
            resultMap.put("msg_signature", "null");
            resultMap.put("encrypt", "null");
            resultMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            resultMap.put("nonce", String.valueOf(ThreadLocalRandom.current().nextLong()));
            return resultMap;
        }
    }

    /**
     * 创建审批实例
     *
     * @param request
     * @param bodyJson
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/processinstance/create", method = RequestMethod.POST)
    public ServiceResult createProcessInstance(HttpServletRequest request, @RequestBody JSONObject bodyJson)
        throws ApiException {
        // 处理"/users/"的POST请求，用来创建User
        // 除了@ModelAttribute绑定参数之外，还可以通过@RequestParam从页面中传递参数
        String accessToken;
        // 获取accessToken
        ServiceResult<String> accessTokenSr = tokenService.getAccessToken();
        if (!accessTokenSr.isSuccess()) {
            return ServiceResult.failure(accessTokenSr.getCode(), accessTokenSr.getMessage());
        }
        accessToken = accessTokenSr.getResult();

        // 审批模板代码
        String processCode = bodyJson.getString("processCode");
        // 审批发起人id
        String originatorUserId = bodyJson.getString("originatorUserId");
        // 部门id
        Long deptId = bodyJson.getLong("deptId");
        // 抄送人ids
        String ccList = bodyJson.getString("ccList");
        // 审批人ids
        List<String> approverUserIds = Arrays.asList(bodyJson.getString("approverUserIds").split(","));

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/create");
        OapiProcessinstanceCreateRequest req = new OapiProcessinstanceCreateRequest();
        req.setAgentId(Long.parseLong(appConfig.getAgentId()));
        req.setProcessCode(processCode);
        req.setOriginatorUserId(originatorUserId);
        req.setDeptId(deptId);
        req.setCcList(ccList);
        req.setCcPosition("FINISH");

        //单行输入框
        List<OapiProcessinstanceCreateRequest.FormComponentValueVo> formComponentValueVoList =
            new ArrayList<OapiProcessinstanceCreateRequest.FormComponentValueVo>();
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo);
        formComponentValueVo.setName("单行输入框");
        formComponentValueVo.setValue("测试单行输入框");

        //多行输入框
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo1 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo1);
        formComponentValueVo1.setName("多行输入框");
        formComponentValueVo1.setValue("测试多行输入框");

        //金额输入框
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo2 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo2);
        formComponentValueVo2.setName("金额（元）大写");
        formComponentValueVo2.setValue("1");

        //数字输入框
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo3 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo3);
        formComponentValueVo3.setName("数字输入框");
        formComponentValueVo3.setValue("100");

        //单选框组件
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo5 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo5);
        formComponentValueVo5.setName("单选框");
        formComponentValueVo5.setValue("a");

        //多选框组件
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo6 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo6);
        formComponentValueVo6.setName("多选框");
        formComponentValueVo6.setValue("[\"a\",\"b\"]");

        //日期组件
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo7 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo7);
        formComponentValueVo7.setName("日期");
        formComponentValueVo7.setValue("2021-08-17");

        //日期区间组件
        OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo8 =
            new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        formComponentValueVoList.add(formComponentValueVo8);
        formComponentValueVo8.setName("[\"开始时间\",\"结束时间\"]");
        formComponentValueVo8.setValue("[\"2019-02-19\",\"2019-02-25\"]");

        //上传图片
        // OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo9 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // formComponentValueVoList.add(formComponentValueVo9);
        // formComponentValueVo9.setName("图片");
        // formComponentValueVo9.setValue(JSON.toJSONString(new String[] {"https://xxxxxxxx", "https://xxxxxxxxx"}));

        //上传审批附件
        // OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo10 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // formComponentValueVoList.add(formComponentValueVo10);
        // JSONObject jsonObject = new JSONObject();
        // jsonObject.put("spaceId", "163xxxx658");
        // jsonObject.put("fileName", "IMG_2322.PNG");
        // jsonObject.put("fileSize", "276297");
        // jsonObject.put("fileType", "png");
        // jsonObject.put("fileId", "405xxxxx777");
        // Object o[] = new Object[] {jsonObject};
        // String s = JSON.toJSONString(o);
        // formComponentValueVo10.setName("附件");
        // formComponentValueVo10.setValue(s);

        //联系人
        // OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo11 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // formComponentValueVoList.add(formComponentValueVo11);
        // formComponentValueVo11.setName("联系人");
        // formComponentValueVo11.setValue("[\"4525xxxxxxxx77041\"]");

        //明细
        // OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo12 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // OapiProcessinstanceCreateRequest.FormComponentValueVo Item1 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // Item1.setName("单行输入框");
        // Item1.setValue("明细单行输入框");
        // OapiProcessinstanceCreateRequest.FormComponentValueVo Item2 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // Item2.setName("数字输入框");
        // Item2.setValue("100");
        // formComponentValueVo12.setName("明细");
        // formComponentValueVo12.setValue(JSON.toJSONString(Arrays.asList(Arrays.asList(Item1, Item2))));
        // formComponentValueVoList.add(formComponentValueVo12);

        //关联审批单
        // OapiProcessinstanceCreateRequest.FormComponentValueVo formComponentValueVo13 =
        //     new OapiProcessinstanceCreateRequest.FormComponentValueVo();
        // formComponentValueVoList.add(formComponentValueVo13);
        // formComponentValueVo13.setName("关联审批单");
        // formComponentValueVo13.setValue(JSON.toJSONString(Arrays.asList("fa2aa864-xxxx-xxxx-xxxx-75572c0e2cdf")));

        //设置审批人，会签、或签设置的审批人必须大于等于2个人
        List<OapiProcessinstanceCreateRequest.ProcessInstanceApproverVo> processInstanceApproverVoList =
            new ArrayList<OapiProcessinstanceCreateRequest.ProcessInstanceApproverVo>();

        OapiProcessinstanceCreateRequest.ProcessInstanceApproverVo processInstanceApproverVo =
            new OapiProcessinstanceCreateRequest.ProcessInstanceApproverVo();
        processInstanceApproverVoList.add(processInstanceApproverVo);
        processInstanceApproverVo.setTaskActionType("AND");
        processInstanceApproverVo.setUserIds(approverUserIds);

        OapiProcessinstanceCreateRequest.ProcessInstanceApproverVo processInstanceApproverVo1 =
            new OapiProcessinstanceCreateRequest.ProcessInstanceApproverVo();
        processInstanceApproverVoList.add(processInstanceApproverVo1);
        processInstanceApproverVo1.setTaskActionType("OR");
        processInstanceApproverVo1.setUserIds(approverUserIds);

        req.setApproversV2(processInstanceApproverVoList);
        req.setFormComponentValues(formComponentValueVoList);
        _logger.info("req={}", JSON.toJSON(req).toString());
        OapiProcessinstanceCreateResponse rsp = client.execute(req, accessToken);
        System.out.println(rsp.getBody());

        return ServiceResult.success(JSON.toJSON(rsp).toString());
    }

    /**
     * 创建审批实例
     *
     * @param request
     * @param bodyJson
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/processinstance/terminate", method = RequestMethod.POST)
    public ServiceResult terminateProcessInstance(HttpServletRequest request, @RequestBody JSONObject bodyJson)
        throws ApiException {
        String accessToken;
        // 获取accessToken
        ServiceResult<String> accessTokenSr = tokenService.getAccessToken();
        if (!accessTokenSr.isSuccess()) {
            return ServiceResult.failure(accessTokenSr.getCode(), accessTokenSr.getMessage());
        }
        accessToken = accessTokenSr.getResult();

        // 审批实例id
        String processInstanceId = bodyJson.getString("processInstanceId");

        DingTalkClient client =
            new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/process/instance/terminate");
        OapiProcessInstanceTerminateRequest req = new OapiProcessInstanceTerminateRequest();
        OapiProcessInstanceTerminateRequest.TerminateProcessInstanceRequestV2 obj1 =
            new OapiProcessInstanceTerminateRequest.TerminateProcessInstanceRequestV2();
        obj1.setProcessInstanceId(processInstanceId);
        obj1.setIsSystem(true);
        obj1.setRemark("撤销审批实例");
        req.setRequest(obj1);
        OapiProcessInstanceTerminateResponse rsp = client.execute(req, accessToken);
        System.out.println(rsp.getBody());

        return ServiceResult.success(JSON.toJSON(rsp).toString());
    }

}