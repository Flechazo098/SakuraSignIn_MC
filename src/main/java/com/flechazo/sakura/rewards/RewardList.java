package com.flechazo.sakura.rewards;

import com.flechazo.sakura.util.CollectionUtils;
import com.google.gson.JsonArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Flechazo
 */
public class RewardList extends ArrayList<Reward> implements Serializable, Cloneable {
    public RewardList() {
    }

    public RewardList(Collection<Reward> collection) {
        super(collection);
    }

    @Override
    public RewardList clone() {
        try {
            RewardList cloned = new RewardList();
            for (Reward reward : this) {
                cloned.add(reward != null ? reward.clone() : null);
            }
            return cloned;
        } catch (Exception e) {
            return new RewardList();
        }
    }

    @Override
    public boolean add(Reward reward) {
        // 不允许添加null奖励，使用默认奖励替代
        return super.add(reward != null ? reward : Reward.getDefault());
    }

    @Override
    public void add(int index, Reward reward) {
        // 不允许添加null奖励，使用默认奖励替代
        super.add(index, reward != null ? reward : Reward.getDefault());
    }

    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();
        for (Reward reward : this) {
            if (reward != null) {
                jsonArray.add(reward.toJsonObject());
            }
        }
        return jsonArray;
    }
}
