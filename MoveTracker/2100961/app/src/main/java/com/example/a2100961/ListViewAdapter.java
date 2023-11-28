package com.example.a2100961;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private ArrayList<ListViewItem> listViewItems = new ArrayList<ListViewItem>();
    int nextNo = 1; // 다음 아이템의 no 값을 추적하기 위한 변수



    public ListViewAdapter() {

    }

    public void deleteItem(int position) {

        listViewItems.remove(position);
        notifyDataSetChanged();
    }

    public void updateItem(int index, ListViewItem element) { listViewItems.set(index, element); }

    public void addItem(String start, String end, String distance) {
        ListViewItem item = new ListViewItem();

        item.setNo(nextNo++); // nextNo를 사용하여 no 값을 1씩 증가시킴
        item.setStart(start);
        item.setEnd(end);
        item.setDistance(distance);


        listViewItems.add(item);

    }





    public void clearItems() { listViewItems.clear(); }


    @Override
    public int getCount() {
        return listViewItems.size();
    }

    @Override
    public ListViewItem getItem(int position) {
        return listViewItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item,
                    parent, false);
        }



        TextView textView4 = (TextView) convertView.findViewById(R.id.textView4);
        TextView textView5 = (TextView) convertView.findViewById(R.id.textView5);
        TextView textView6 = (TextView) convertView.findViewById(R.id.textView6);
        TextView textView10 = (TextView) convertView.findViewById(R.id.textView10);

        ListViewItem item = listViewItems.get(position);

        textView4.setText(String.valueOf(item.getNo()));
        textView5.setText(item.getStart());
        textView6.setText(item.getEnd());
        textView10.setText(item.getDistance());


        textView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 원하는 크기로 조절
        textView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView10.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);


        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("아이템 삭제")
                        .setMessage(textView4.getText() + "번 삭제 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItem(position);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // 취소 버튼을 누르면 대화 상자를 닫음
                            }
                        })
                        .show();
                return true;
            }
        });


        return convertView;
    }


}
