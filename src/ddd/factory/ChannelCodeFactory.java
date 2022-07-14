package com.ddd.factory;

import com.ddd.entity.ChannelCodeE;

import java.util.ArrayList;
import java.util.List;

/**
 * 渠道码领域工厂
 *
 * @author CaiZelin
 * @date 2022/2/22 15:45
 */
@Component
public class ChannelCodeFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(ChannelCodeFactory.class);

    @Autowired
    private RpcTeamService rpcTeamService;

    @Autowired
    private AgentChannelDAO agentChannelDAO;

    @Autowired
    private TeamAgentChannelDAO teamAgentChannelDAO;


    /**
     * 构建渠道码领域实体
     *
     * @param agentId 企业号或个人号的经纪人Id
     * @return
     */
    public ChannelCodeE buildChannelCodeE(AgentId agentId) {

        AgentBaseInfoRespVO agentBaseInfo = BResponseDataUtils.getData(() -> rpcTeamService.getAgentBaseInfo(agentId.get()));
        if (null == agentBaseInfo) {
            LOGGER.error("agentId {} is invalid", agentId.get());
            Exceptions.throwss(ErrorMsg.MSG_MS_ERROR);

        }

        String sourceAgent = null;
        String sourceTeamId = null;
        String channelCode = null;
        List<String> joinAgents = new ArrayList<>();


        // 企业号
        if (StringUtils.isNotEmpty(agentBaseInfo.getTeamId())) {

            sourceAgent = agentBaseInfo.getAgentId();
            sourceTeamId = agentBaseInfo.getTeamId();
            channelCode = getChannelCode(sourceAgent);
            joinAgents = getJoinAgents(channelCode);
        } else { // 个人号


            // 个人号先查询是否关联渠道码
            AgentChannelDocument agentChannelDoc = agentChannelDAO.getByAgentId(agentId.get());

            // 已关联渠道码
            if (null != agentChannelDoc) {
                sourceAgent = agentChannelDoc.getChannelSourceAgentId();
                sourceTeamId = agentChannelDoc.getChannelSourceTeamId();
                channelCode = agentChannelDoc.getChannelCode();
                joinAgents = getJoinAgents(channelCode);
            }

        }

        return new ChannelCodeE(sourceAgent, channelCode, sourceTeamId, joinAgents);

    }

    /**
     * 构建渠道码领域实体
     *
     * @param channelCode 渠道码
     * @return
     */
    public ChannelCodeE buildChannelCodeE(String channelCode) {

        // 1. 查询企业号经纪人渠道表
        TeamAgentChannelDocument channeInfo = teamAgentChannelDAO.getByChannelCode(channelCode);

        // 2. 查询不到渠道码的信息，直接报错
        if (null == channeInfo) {
            LOGGER.error("channelCode {} is invalid", channelCode);
            Exceptions.throwss(ErrorMsg.MSG_ERROR_CHANNEL_CODE_ERROR);
        }

        String sourceAgent = channeInfo.getAgentId();
        String sourceTeamId = channeInfo.getTeamId();
        List<String> joinAgents = getJoinAgents(channelCode);

        return new ChannelCodeE(sourceAgent, channelCode, sourceTeamId, joinAgents);

    }


    /**
     * 获取企业经纪人渠道码
     *
     * @param sourceAgentId 企业经纪人的Id
     * @return
     */
    public String getChannelCode(String sourceAgentId) {
        TeamAgentChannelDocument document = teamAgentChannelDAO.getBySourceAgentId(sourceAgentId);
        if (null != document) {
            return document.getChannelCode();
        }

        return null;
    }


    /**
     * 获取加入当前渠道码的个人号经纪人ID集合
     *
     * @param channelCode 渠道码Id
     * @return
     */
    public List<String> getJoinAgents(String channelCode) {

        List<String> joinAgents = agentChannelDAO.getAgentIdsByChannelCode(channelCode);

        return joinAgents;
    }
}
