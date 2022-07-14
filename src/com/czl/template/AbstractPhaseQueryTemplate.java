package com.czl.template;

import com.czl.utils.ZlOptional;
import java.util.ArrayList;
import java.util.List;

/**
 * 分阶段查询模板
 *
 * @param <T>
 */
public abstract class AbstractPhaseQueryTemplate<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPhaseQueryTemplate.class);

    private static final String SEPARATOR = "_";

    /**
     * 查询阶段列表
     *
     * @return
     */
    protected abstract List<PhaseQuery<T>> phaseQueries();

    /**
     * 获取列表
     * <p>
     * 查询数据源由所有的查询阶段[阶段1, 阶段2, 阶段3, ...]组合而成，依次遍历每一个查询阶段，直到获取到所需要的数据或者没有下一阶段为止
     * <p>
     * 对于某一个查询阶段来说，存在下面3种情况
     * 1: 该阶段的数据足够用于本次查询，则获取到查询结果后，退出本次查询
     * 2: 该阶段的数据是本次查询的一部分，继续流转到下一阶段，没有下一阶段则退出
     * 3：该阶段的数据不包含本次查询的数据，继续流转到下一阶段，没有下一阶段则退出
     *
     * @param nextId 查询阶段索引下标_跳过数量
     * @param limit  页数
     * @return
     */
    public PhaseQueryResult<T> getList(String nextId, int limit) {
        StopWatch started = StopWatch.createStarted();

        LOGGER.info("AbstractPhaseQueryTemplate getList start nextId {} limit {}", nextId, limit);

        int index = getIndex(nextId);

        int skip = getSkip(nextId);

        List<PhaseQuery<T>> phaseQueries = phaseQueries();

        List<T> result = new ArrayList<>();

        if (!validateParam(index, skip, limit, phaseQueries)) {
            return new PhaseQueryResult<>(result);
        }

        for (int i = index; i < phaseQueries.size(); i++) {

            PhaseQuery<T> phaseQuery = phaseQueries.get(i);

            List<T> list = ZlOptional.ofList(phaseQuery.query(skip, limit));

            int count = list.size();

            if (count == limit) {

                result.addAll(list);

                LOGGER.info("AbstractPhaseQueryTemplate getList end cost {}", started.stop());

                return new PhaseQueryResult<>(result, genNextId(i, skip + limit));

            } else if (count > 0) {

                result.addAll(list);

                limit = limit - count;

                skip = 0;

            } else {

                skip = 0;
            }
        }

        LOGGER.info("AbstractPhaseQueryTemplate getList end cost {}", started.stop());

        return new PhaseQueryResult<>(result);
    }

    /**
     * 参数校验
     *
     * @param index
     * @param skip
     * @param limit
     * @param phaseQueries
     * @return
     */
    private boolean validateParam(int index, int skip, int limit, List<PhaseQuery<T>> phaseQueries) {
        if (index < 0 || skip < 0 || limit < 0) {
            return false;
        }

        return !CollectionUtils.isEmpty(phaseQueries);
    }

    /**
     * 根据nextId获取跳过条数
     *
     * @param nextId
     * @return
     */
    private int getSkip(String nextId) {
        if (StringUtils.isEmpty(nextId)) {
            return 0;
        }
        return Integer.parseInt(nextId.substring(nextId.indexOf(SEPARATOR) + 1));
    }

    /**
     * 根据nextId获取查询阶段索引下标
     *
     * @param nextId
     * @return
     */
    private int getIndex(String nextId) {
        if (StringUtils.isEmpty(nextId)) {
            return 0;
        }
        return Integer.parseInt(nextId.substring(0, nextId.indexOf(SEPARATOR)));
    }

    /**
     * 生成nextId
     *
     * @param index
     * @param skip
     * @return
     */
    private String genNextId(int index, int skip) {
        return index + SEPARATOR + skip;
    }

    /**
     * 查询结果
     *
     * @param <T>
     */
    public static class PhaseQueryResult<T> {

        private List<T> list;

        private String nextId;

        public PhaseQueryResult(List<T> list) {
            this.list = list;
        }

        public PhaseQueryResult(List<T> list, String nextId) {
            this.list = list;
            this.nextId = nextId;
        }

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }

        public String getNextId() {
            return nextId;
        }

        public void setNextId(String nextId) {
            this.nextId = nextId;
        }

        @Override
        public String toString() {
            return "PhaseQueryResult{" +
                    "list=" + list +
                    ", nextId='" + nextId + '\'' +
                    '}';
        }
    }

    /**
     * 查询阶段定义
     *
     * @param <T>
     */
    public interface PhaseQuery<T> {

        /**
         * 查询
         *
         * @param skip
         * @param limit
         * @return
         */
        List<T> query(int skip, int limit);
    }
}
