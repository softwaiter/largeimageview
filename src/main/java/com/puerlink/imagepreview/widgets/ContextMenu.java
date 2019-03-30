package com.puerlink.imagepreview.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.puerlink.imagepreview.R;
import com.puerlink.imagepreview.utils.CommonUtils;

public class ContextMenu {

	public interface OnMenuItemClickListener
	{
		public void onMenuItemClickListener(int position, String label);
	}

	private Context m_Context;

	private ListView m_ItemsView;

	private ContextMenuAdapter m_ItemsAdapter;

	private PopupWindow m_Menu;

	private List<String> m_Items = new ArrayList<String>();

	private OnMenuItemClickListener m_MenuItemClickListener;

	public ContextMenu(Context context, int menuWidth, int animStyle) {
		m_Context = context;

		LayoutInflater li = LayoutInflater.from(context);
		View view = li.inflate(R.layout.view_context_menu, null);

		m_ItemsView = (ListView)view.findViewById(R.id.list_menu);
		m_ItemsView.setFocusable(false);
		m_ItemsView.setFocusableInTouchMode(true);

		m_ItemsAdapter = new ContextMenuAdapter();
		m_ItemsView.setAdapter(m_ItemsAdapter);

		m_ItemsView.setOnItemClickListener(menuItemClickListener);
		m_ItemsView.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg1 == KeyEvent.KEYCODE_MENU)
				{
					if (m_Menu != null)
					{
						if (m_Menu.isShowing())
						{
							m_Menu.dismiss();
						}
					}
				}
				return false;
			}

		});

		m_Menu = new PopupWindow(view, menuWidth, LayoutParams.WRAP_CONTENT, false);
		m_Menu.setBackgroundDrawable(new BitmapDrawable());
		if (animStyle > 0)
		{
			m_Menu.setAnimationStyle(animStyle);
		}
		m_Menu.setOutsideTouchable(true);
		m_Menu.setFocusable(true);
		m_Menu.update();
	}

	AdapterView.OnItemClickListener menuItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
								long arg3) {

			if (m_Menu != null)
			{
				if (m_Menu.isShowing())
				{
					m_Menu.dismiss();
				}
			}

			if (m_MenuItemClickListener != null)
			{
				m_MenuItemClickListener.onMenuItemClickListener(arg2, m_Items.get(arg2));
			}
		}

	};

	public ContextMenu(Context context, int menuWidth)
	{
		this(context, menuWidth, 0);
	}

	public ContextMenu(Context context)
	{
		this(context, CommonUtils.dp2px(context, 150));
	}

	public void addItem(String item)
	{
		m_Items.add(item);
		m_ItemsAdapter.notifyDataSetChanged();
	}

	public void addItem(int resId)
	{
		addItem(m_Context.getString(resId));
	}

	public void insertItem(int position, String item)
	{
		m_Items.add(position, item);
		m_ItemsAdapter.notifyDataSetChanged();
	}

	public void removeItem(int position)
	{
		m_Items.remove(position);
		m_ItemsAdapter.notifyDataSetChanged();
	}

	public void updateItem(int position, String text)
	{
		if (position >= 0 && position < m_Items.size())
		{
			m_Items.add(position, text);
			m_Items.remove(position + 1);
			m_ItemsAdapter.notifyDataSetChanged();
		}
	}

	public void clear()
	{
		m_Items.clear();
		m_ItemsAdapter.notifyDataSetChanged();
	}

	public void showInCenter(View anchor)
	{
		if (m_Menu != null)
		{
			if (m_Menu.isShowing())
			{
				m_Menu.dismiss();
			}
			else
			{
				m_Menu.showAtLocation(anchor, Gravity.CENTER, 0, 0);
			}
		}
	}

	public void show(View anchor, int xoff, int yoff)
	{
		if (m_Menu != null)
		{
			if (m_Menu.isShowing())
			{
				m_Menu.dismiss();
			}
			else
			{
				m_Menu.showAsDropDown(anchor, xoff, yoff);
			}
		}
	}

	public void show(View anchor)
	{
		show(anchor, 0, 0);
	}

	public void close()
	{
		if (m_Menu != null) {
			m_Menu.dismiss();
		}
	}

	public void setOnMenuItemClickListener(OnMenuItemClickListener listener)
	{
		m_MenuItemClickListener = listener;
	}

	class ContextMenuAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			return m_Items.size();
		}

		@Override
		public Object getItem(int position) {
			return m_Items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView itemLabel;
			if (convertView == null)
			{
				convertView=LayoutInflater.from(m_Context).inflate(R.layout.view_context_menu_item, null);
			}
			itemLabel = (TextView)convertView.findViewById(R.id.text_label);
			itemLabel.setText(m_Items.get(position));

			return convertView;
		}

	}
}
