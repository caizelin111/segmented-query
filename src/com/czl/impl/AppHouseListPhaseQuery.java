package com.czl.impl;

import com.czl.model.TeamHouseDocument;
import com.czl.template.AbstractPhaseQueryTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * APP房源列表阶段查询
 *
 * @author CaiZelin
 * @date 2022/5/23 16:48
 */
public class AppHouseListPhaseQuery extends AbstractPhaseQueryTemplate<TeamHouseDocument> {

//    private static final Logger LOGGER = LoggerFactory.getLogger(AppHouseListPhaseQuery.class);

    // init
    public AppHouseListPhaseQuery() {
        // init - param
        // this.teamHouseDAO = SpringUtils.getBean(TeamHouseDAO.class);

    }

    @Override
    protected List<PhaseQuery<TeamHouseDocument>> phaseQueries() {
        return Arrays.asList(new CollectedHouseQuery(), new NonCollectedHouseQuery());
    }


    /**
     * 查询收藏的房源
     */
    class CollectedHouseQuery implements PhaseQuery<TeamHouseDocument> {

        @Override
        public List<TeamHouseDocument> query(int skip, int limit) {
            return null;
        }
    }

    /**
     * 查询非收藏的房源
     */
    class NonCollectedHouseQuery implements PhaseQuery<TeamHouseDocument> {

        @Override
        public List<TeamHouseDocument> query(int skip, int limit) {
            return null;
        }
    }

}
