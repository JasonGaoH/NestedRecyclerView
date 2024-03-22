package com.gaohui.nestedrecyclerview.update.listener;

import android.support.v7.widget.RecyclerView;

import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;

public abstract class AbsNestedRVOnScrollListener extends RecyclerView.OnScrollListener {
    abstract public void setVelocityY(int velocityY);

    abstract public void updatePullState(UpdatedNestedRecyclerView parentRV);
}
