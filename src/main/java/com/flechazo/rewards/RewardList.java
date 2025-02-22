package com.flechazo.rewards;

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
        RewardList cloned = (RewardList) super.clone();
        List<Reward> clonedRewards = new ArrayList<>();
        for (Reward reward : this) {
            if (reward != null) {
                clonedRewards.add(reward.clone());
            } else {
                // 如果遇到null，使用默认奖励替代
                clonedRewards.add(Reward.getDefault());
            }
        }
        cloned.clear();
        cloned.addAll(clonedRewards);
        return cloned;
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
