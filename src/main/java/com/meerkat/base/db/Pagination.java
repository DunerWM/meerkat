package com.meerkat.base.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meerkat.base.util.JsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pagination<T> implements Serializable {


    public enum Mode {
        FULL, NEXT_ONLY
    }

    /**
     * 页数从1开始.
     */
    @JsonProperty
    public int page;
    @JsonProperty
    public int perPage;
    @JsonProperty
    public int total;
    @JsonProperty
    public boolean hasNext;
    @JsonProperty
    public Mode mode;
    @JsonProperty
    private List<T> data = new ArrayList<T>();

    public Pagination() {
    }

    public Pagination(int page, int perPage, int total) {
        this.page = page < 1 ? 1 : page;
        this.perPage = perPage;
        this.total = total;
        this.mode = Mode.FULL;
        this.hasNext = page < totalPages();
    }

    public Pagination(int page, boolean hasNext) {
        this.page = page;
        this.hasNext = hasNext;
        this.mode = Mode.NEXT_ONLY;
        this.perPage = -1;
        this.total = -1;
    }

    public void setData(List<T> data) {
        this.data.clear();
        this.data.addAll(data);
    }

    public List<T> getData() {
        return data;
    }

    @JsonProperty("totalPages")
    public int totalPages() {
        return (total + perPage - 1) / perPage;
    }

    /**
     * start from 1;
     *
     * @return
     */
    public int offset() {
        return (page - 1) * perPage + 1;
    }

    public int nextPage() {
        if (page < totalPages()) {
            return page + 1;
        }
        return -1;
    }

    public int previousPage() {
        return page <= 1 ? -1 : page - 1;
    }

    public <V> Pagination<V> replace(List<V> data) {
        Pagination<V> result = new Pagination<V>(page, perPage, total);
        if (mode == Mode.NEXT_ONLY) {
            result = new Pagination<>(page, hasNext);
        }
        result.setData(data);
        return result;
    }

    public List<Integer> pageNavigation() {
        List<Integer> pages = new ArrayList<Integer>();
        int min = 1;
        int max = totalPages();

        int innerWindow = 2;
        int from = Math.max(min, page - innerWindow);
        int to = Math.min(max, page + innerWindow);

        boolean leftGap = min + 2 < from;
        boolean rightGap = to + 2 < max;
        if (!leftGap) {
            from = min;
        }
        if (!rightGap) {
            to = max;
        }

        if (min != from) {
            pages.add(min);
        }
        if (leftGap) {
            pages.add(-1);
        }
        for (int i = from; i <= to; i++) {
            pages.add(i);
        }
        if (rightGap) {
            pages.add(-1);
        }
        if (max != to) {
            pages.add(max);
        }

        return pages;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getTotal() {
        return total;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "Pagination [page=" + page + ", perPage=" + perPage + ", total="
                + total + ", data=" + data + "]";
    }

    public String toJson() {
        return JsonUtil.dump(this);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("perPage", perPage);
        map.put("total", total);
        map.put("hasNext", hasNext);
        map.put("mode", mode);
        map.put("totalPages", totalPages());
        map.put("data", data);
        return map;
    }

}
