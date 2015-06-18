package com.ready.readyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

/**
 * Created by clement on 16/06/15.
 */

public class PersonListAdapter extends BaseAdapter {

    private List<Person> listPerson = null;
    LayoutInflater layoutInflater;

    // constructeur
    public PersonListAdapter(Context context, List<Person> listPerson) {
        this.listPerson = listPerson;
        layoutInflater = LayoutInflater.from(context);
        this.listPerson = listPerson;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listPerson.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return listPerson.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    static class ViewHolder {
        TextView nameView;
        TextView statusView;
        RelativeTimeTextView dateView;
        ImageView onlineView;
        ImageView pictureView;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.people, parent, false);
            holder = new ViewHolder();
            // initialisation des vues
            holder.nameView = (TextView) convertView.findViewById(R.id.textView);
            holder.statusView = (TextView) convertView
                    .findViewById(R.id.status);
            holder.pictureView = (ImageView) convertView
                    .findViewById(R.id.picture);
            holder.onlineView = (ImageView) convertView
                    .findViewById(R.id.online);

            holder.dateView = (RelativeTimeTextView) convertView.findViewById(R.id.updatedTime);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // affchier les données convenablement dans leurs positions
        holder.nameView.setText(listPerson.get(position).getName());
        holder.statusView.setText(String.valueOf(listPerson.get(position)
                .getStatus()));
        holder.pictureView.setBackgroundDrawable(listPerson.get(position)
                .getPicture());
        holder.onlineView.setBackgroundDrawable(listPerson.get(position)
                .getOnline());
        holder.dateView.setReferenceTime(listPerson.get(position).getUpdatedAt());
        return convertView;

    }
}
