/*
 * Copyright 2017 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yanzhenjie.recyclerview.x.R;

import java.lang.reflect.Field;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.yanzhenjie.recyclerview.SwipeRecyclerView.LEFT_DIRECTION;
import static com.yanzhenjie.recyclerview.SwipeRecyclerView.RIGHT_DIRECTION;

/**
 * Created by YanZhenjie on 2017/7/20.
 */
class AdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView.Adapter mAdapter;
    private LayoutInflater mInflater;

    private SwipeMenuCreator mSwipeMenuCreator;
    private OnItemMenuClickListener mOnItemMenuClickListener;

    AdapterWrapper(Context context, RecyclerView.Adapter adapter) {
        this.mInflater = LayoutInflater.from(context);
        this.mAdapter = adapter;
    }

    public RecyclerView.Adapter<?> getOriginAdapter() {
        return mAdapter;
    }

    /**
     * Set to create menu listener.
     *
     * @param swipeMenuCreator listener.
     */
    void setSwipeMenuCreator(SwipeMenuCreator swipeMenuCreator) {
        this.mSwipeMenuCreator = swipeMenuCreator;
    }

    /**
     * Set to click menu listener.
     *
     * @param onItemMenuClickListener listener.
     */
    void setOnItemMenuClickListener(OnItemMenuClickListener onItemMenuClickListener) {
        this.mOnItemMenuClickListener = onItemMenuClickListener;
    }


    @Override
    public int getItemCount() {
        return  getContentItemCount();
    }

    private int getContentItemCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final RecyclerView.ViewHolder viewHolder = mAdapter.onCreateViewHolder(parent, viewType);

        if (mSwipeMenuCreator == null) return viewHolder;

        View   contentView = mInflater.inflate(R.layout.x_recycler_view_item, parent, false);
        ViewGroup viewGroup = contentView.findViewById(R.id.swipe_content);
        viewGroup.addView(viewHolder.itemView);

        try {
            Field itemView = getSupperClass(viewHolder.getClass()).getDeclaredField("itemView");
            if (!itemView.isAccessible()) itemView.setAccessible(true);
            itemView.set(viewHolder, contentView);
        } catch (Exception ignored) {
        }
        return viewHolder;
    }

    private Class<?> getSupperClass(Class<?> aClass) {
        Class<?> supperClass = aClass.getSuperclass();
        if (supperClass != null && !supperClass.equals(Object.class)) {
            return getSupperClass(supperClass);
        }
        return aClass;
    }

    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
        @NonNull List<Object> payloads) {

        View itemView = holder.itemView;

        if (itemView instanceof SwipeMenuLayout && mSwipeMenuCreator != null) {
            SwipeMenuLayout menuLayout = (SwipeMenuLayout)itemView;
            SwipeMenu leftMenu = new SwipeMenu(menuLayout);
            SwipeMenu rightMenu = new SwipeMenu(menuLayout);
            mSwipeMenuCreator.onCreateMenu(leftMenu, rightMenu, position);

            SwipeMenuView leftMenuView = (SwipeMenuView)menuLayout.getChildAt(0);
            if (leftMenu.hasMenuItems()) {
                leftMenuView.setOrientation(leftMenu.getOrientation());
                leftMenuView.createMenu(leftMenu, menuLayout, LEFT_DIRECTION);
            } else if (leftMenuView.getChildCount() > 0) {
                leftMenuView.removeAllViews();
            }

            SwipeMenuView rightMenuView = (SwipeMenuView)menuLayout.getChildAt(2);
            if (rightMenu.hasMenuItems()) {
                rightMenuView.setOrientation(rightMenu.getOrientation());
                rightMenuView.createMenu(rightMenu, menuLayout, RIGHT_DIRECTION);
            } else if (rightMenuView.getChildCount() > 0) {
                rightMenuView.removeAllViews();
            }
        }

        mAdapter.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm instanceof GridLayoutManager) {
            final GridLayoutManager glm = (GridLayoutManager)lm;
            final GridLayoutManager.SpanSizeLookup originLookup = glm.getSpanSizeLookup();

            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (originLookup != null) return originLookup.getSpanSize(position);
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        mAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public final void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        mAdapter.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
       return mAdapter.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
     mAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter.onDetachedFromRecyclerView(recyclerView);
    }
}