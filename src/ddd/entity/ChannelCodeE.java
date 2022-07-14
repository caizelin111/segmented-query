package com.ddd.entity;

import com.ddd.factory.ChannelCodeFactory;

import java.util.List;

/**
 * 渠道码领域实体
 *
 * @author CaiZelin
 * @date 2022/2/22 15:18
 */

public class ChannelCodeE {
    private static Logger LOGGER = LoggerFactory.getLogger(ChannelCodeE.class);

    private String sourceAgent;                 // 来源渠道的经纪人
    private String channelCode;                 // 渠道码
    private String sourceTeamId;                // 企业id
    private List<String> joinAgents;            // 加入渠道的经纪人

    public ChannelCodeE() {
    }

    public ChannelCodeE(String sourceAgent, String channelCode, String sourceTeamId, List<String> joinAgents) {
        this.sourceAgent = sourceAgent;
        this.channelCode = channelCode;
        this.sourceTeamId = sourceTeamId;
        this.joinAgents = joinAgents;
    }

    public String getSourceAgent() {
        return sourceAgent;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getSourceTeamId() {
        return sourceTeamId;
    }

    public List<String> getJoinAgents() {
        return joinAgents;
    }


    /**
     * 生成企业经纪人渠道码
     *
     * @param teamAgentChannelDAO
     * <p>
     * 该方法必须由 {@link ChannelCodeFactory#buildChannelCodeE(com.fanggeek.cayenne.service.domain.id.AgentId)} 生成的领域对象调用
     */
    public void genChannelCode(TeamAgentChannelDAO teamAgentChannelDAO) {

        // 1. 校验
        if(StringUtils.isBlank(sourceAgent)) {
            LOGGER.error("sourceAgentId not existed {}", sourceAgent);
            Exceptions.throwss(ErrorMsg.MSG_ERROR_CHANNEL_SOURCE_AGENT_NOT_EXIST);
        }

        if (StringUtils.isNotBlank(this.channelCode)) {
            LOGGER.error("channelCode has existed {}", channelCode);
            Exceptions.throwss(ErrorMsg.MSG_ERROR_CHANNEL_CODE_HAS_EXITED);
        }

        // 2. 生成渠道码 （把企业agentId 转 62进制字符串）
        String channelCode = ConversionBase62Util.encode62(sourceAgent);

        // 3. 保存渠道码
        TeamAgentChannelDocument document = new TeamAgentChannelDocument();
        document.setAgentId(sourceAgent);
        document.setTeamId(sourceTeamId);
        document.setChannelCode(channelCode);
        teamAgentChannelDAO.save(document);

        // 4. 赋值渠道码
        this.channelCode = channelCode;
    }


    /**
     * 个人号绑定渠道码
     * <br>
     * 该方法必须由 {@link ChannelCodeFactory#buildChannelCodeE(java.lang.String)} 生成的领域对象调用
     *
     * @param agentId 个人号Id
     */
    public void bindChannelCode(String agentId, RpcTeamService rpcTeamService, AgentChannelDAO agentChannelDAO) {
        if(StringUtils.isBlank(this.channelCode)) {
            LOGGER.error("channelCode is null");
            Exceptions.throwss(ErrorMsg.MSG_ERROR_CHANNEL_CODE_IS_NULL);
        }

        if (joinAgents.stream().anyMatch(one -> one.equals(agentId))) {
            LOGGER.error("agentId {} has bound channelCode", agentId);
            Exceptions.throwss(ErrorMsg.MSG_ERROR_CHANNEL_AGENT_HAS_BOUND_CODE);
        }


        // 1. 绑定渠道码 根据 this.channelCode 和 agentId 进行绑定
        AgentChannelDocument document = new AgentChannelDocument();
        document.setAgentId(agentId);

        AgentBaseInfoRespVO data = BResponseDataUtils.getData(() -> rpcTeamService.getAgentBaseInfo(agentId));
        if (null == data) {
            LOGGER.error("agentId {} rpc get agent info error", agentId);
            Exceptions.throwss(ErrorMsg.MSG_MS_ERROR);
        }
        document.setCompanyName(data.getCompany());
        document.setPhone(null == data.getPhone() ? data.getPhone2() : data.getPhone());

        document.setChannelCode(channelCode);
        document.setChannelSourceAgentId(sourceAgent);


        TeamUserInfoRespVO data2 = BResponseDataUtils.getData(() -> rpcTeamService.getTeamUserInfoVO(sourceAgent));
        if (null == data) {
            LOGGER.error("agentId {} rpc get teamUser info error", sourceAgent);
            Exceptions.throwss(ErrorMsg.MSG_MS_ERROR);
        }
        document.setChannelSourceAgentName(data2.getAgentName());
        document.setChannelSourceTeamName(data2.getTeamName());
        document.setChannelSourceTeamId(sourceTeamId);

        agentChannelDAO.save(document);

        this.joinAgents.add(agentId);
    }


    /**
     * 个人号解绑渠道码
     *
     * @param agentId               个人号经纪人ID
     * @param agentChannelDAO
     * @return
     */
    public void unBindChannelCode(String agentId, AgentChannelDAO agentChannelDAO) {

        if (!joinAgents.stream().anyMatch(one -> one.equals(agentId))) {
            LOGGER.error("agentId {} has not bound channelCode", agentId);
            Exceptions.throwss(ErrorMsg.MSG_ERROR_CHANNEL_AGENT_HAS_NOT_BOUND_CODE);
        }

        agentChannelDAO.delOneSoftByAgentId(agentId);

        this.joinAgents.remove(agentId);
    }

}
