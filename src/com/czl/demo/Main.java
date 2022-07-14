package com.czl.demo;

import com.czl.impl.AppHouseListPhaseQuery;
import com.czl.model.TeamHouseDocument;

import java.util.List;

/**
 * @author CaiZelin
 * @date 2022/7/5 19:58
 */
public class Main {
    private static final Integer PAGE_SIZE_10 = 10;

    public static void main(String[] args) {
        String nextId = null;
        AppHouseListPhaseQuery.PhaseQueryResult<TeamHouseDocument> phaseQueryResult =
                new AppHouseListPhaseQuery().getList(nextId, PAGE_SIZE_10);

        // result
        List<TeamHouseDocument> list = phaseQueryResult.getList();

        // nextId
        nextId = phaseQueryResult.getNextId();

    }
}
