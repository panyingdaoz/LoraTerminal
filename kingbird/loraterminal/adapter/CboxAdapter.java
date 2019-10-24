package com.kingbird.loraterminal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kingbird.loraterminal.R;
import com.kingbird.loraterminal.entity.CboxStatuEntity;
import com.kingbird.loraterminal.utils.Plog;
import com.socks.library.KLog;

import java.util.List;

/**
 * 说明：
 *
 * @author Pan Yingdao
 * @time : 2019/8/16/016
 */
public class CboxAdapter extends ArrayAdapter<CboxStatuEntity> {

    private int resurceId;

    public CboxAdapter(Context context, int resource, List<CboxStatuEntity> objects) {
        super(context, resource, resource, objects);
        resurceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CboxStatuEntity cboxEntity = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resurceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cboxId = view.findViewById(R.id.tv);
            viewHolder.state = view.findViewById(R.id.tv2);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        assert cboxEntity != null;
        String cboxId = cboxEntity.getCboxId();
        String state = cboxEntity.getState();
        Plog.e("每次更新的Item", cboxId + "  " + state);
        viewHolder.cboxId.setText(cboxId);
        viewHolder.state.setText(state);

        return view;
    }

    class ViewHolder {
        TextView cboxId;
        TextView state;
    }
}
