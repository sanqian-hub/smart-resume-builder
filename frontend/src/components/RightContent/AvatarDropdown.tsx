import {
  LogoutOutlined,
  SettingOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import type { MenuProps } from 'antd';
import { Spin } from 'antd';
import { createStyles } from 'antd-style';
import React from 'react';
import { flushSync } from 'react-dom';
import { outLogin } from '@/services/ant-design-pro/api';
import HeaderDropdown from '../HeaderDropdown';

export type GlobalHeaderRightProps = {
  menu?: boolean;
  children?: React.ReactNode;
};

export const AvatarName = () => {
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState || {};
  return <span className="anticon">{currentUser?.username}</span>;
};

const useStyles = createStyles(({ token }) => {
  return {
    action: {
      display: 'flex',
      height: '48px',
      marginLeft: 'auto',
      overflow: 'hidden',
      alignItems: 'center',
      padding: '0 8px',
      cursor: 'pointer',
      borderRadius: token.borderRadius,
      '&:hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
  };
});

export const AvatarDropdown: React.FC<GlobalHeaderRightProps> = ({
  menu,
  children,
}) => {
  /**
   * 退出登录，并且将当前的 url 保存
   */
  const loginOut = async () => {
    await outLogin();
    const { search, pathname } = window.location;
    const urlParams = new URL(window.location.href).searchParams;
    const searchParams = new URLSearchParams({
      redirect: pathname + search,
    });
    /** 此方法会跳转到 redirect 参数所在的位置 */
    const redirect = urlParams.get('redirect');
    // Note: There may be security issues, please note
    if (window.location.pathname !== '/user/login' && !redirect) {
      history.replace({
        pathname: '/user/login',
        search: searchParams.toString(),
      });
    }
  };
  const { styles } = useStyles();

  const { initialState, setInitialState } = useModel('@@initialState');

  const onMenuClick: MenuProps['onClick'] = (event) => {
    const { key } = event;
    if (key === 'logout') {
      flushSync(() => {
        setInitialState((s) => ({ ...s, currentUser: undefined }));
      });
      loginOut();
      return;
    }
    history.push(`/user/${key}`);
  };

  const loading = (
    <span className={styles.action}>
      <Spin
        size="small"
        style={{
          marginLeft: 8,
          marginRight: 8,
        }}
      />
    </span>
  );

  if (!initialState) {
    return loading;
  }

  const { currentUser } = initialState;

  if (!currentUser || !currentUser.username) {
    return loading;
  }

  const menuItems = [
    {
      key: 'user-info',
      label: (
        <div style={{ display: 'flex', alignItems: 'center', padding: '2px 0' }}>
          {/* 复用你的头像 children，缩小显示 */}
          <div style={{ marginRight: 5 }}>
            {React.cloneElement(children as React.ReactElement<{ size?: number }>, { size: 32 })}
          </div>
          <div>
            {/* 用户名：GitHub 样式 */}
            <div style={{
              fontWeight: 600, // GitHub 用户名是 600 粗体
              fontSize: 15,    // 字号 14px
              lineHeight: '1.5', // 行高 1.5
              color: '#1F2328', // GitHub 主文字色
              fontFamily: '-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif' // GitHub 字体
            }}>{currentUser.username}</div>
            {/* 账号/邮箱：GitHub 样式 */}
            <div style={{
              fontWeight: 500, // 普通字重
              fontSize: 13,    // 字号 12px
              lineHeight: '1.5',
              color: '#59636E', // GitHub 次要文字色
              fontFamily: '-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif'
            }}>{currentUser.userAccount || currentUser.username}</div>
          </div>
          {/*/!* 右侧切换账号按钮（模仿 GitHub） *!/*/}
          {/*<div style={{ marginLeft: 'auto', paddingLeft: 16 }}>*/}
          {/*  <span style={{ fontSize: 18, cursor: 'pointer' }}>⇄</span>*/}
          {/*</div>*/}
        </div>
      ),
      disabled: true, // 不可点击，纯展示
    },

    {
      type: 'divider' as const,
      style: {
        borderTop: '2px solid #FF8C3A',
      },
    },
    ...(menu
      ? [
          {
            key: 'center',
            icon: <UserOutlined />,
            label: '个人中心',
            path: '/user/center',
          },
          {
            key: 'settings',
            icon: <SettingOutlined />,
            label: '个人设置',
          },
          {
            type: 'divider' as const,
            style: {
              borderTop: '2px solid #FF8C3A',
            },
          },
        ]
      : []),
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
    },
  ];

  return (
    <HeaderDropdown
      menu={{
        selectedKeys: [],
        onClick: onMenuClick,
        items: menuItems,
      }}
    >
      {children}
    </HeaderDropdown>
  );
};
