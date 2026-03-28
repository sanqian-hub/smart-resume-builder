import {Footer} from '@/components';
import {register} from '@/services/ant-design-pro/api';
import {
  LockOutlined,
  UserOutlined,
  CodeOutlined,
} from '@ant-design/icons';
import {LoginForm, ProFormText,} from '@ant-design/pro-components';
import {Helmet} from '@umijs/max';
import {Alert, App, Button, Tabs} from 'antd';
import {createStyles} from 'antd-style';
import React, {useState} from 'react';
import Settings from '../../../../config/defaultSettings';
import {SYSTEM_LOGO} from "@/constants";

const useStyles = createStyles(({token}) => {
  return {
    action: {
      marginLeft: '8px',
      color: 'rgba(0, 0, 0, 0.2)',
      fontSize: '24px',
      verticalAlign: 'middle',
      cursor: 'pointer',
      transition: 'color 0.3s',
      '&:hover': {
        color: token.colorPrimaryActive,
      },
    },
    lang: {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    },
  };
});

const Register: React.FC = () => {
  const [type, setType] = useState<string>('account');
  const {styles} = useStyles();
  const {message} = App.useApp();
  const handleSubmit = async (values: API.RegisterParams) => {

    // 1. 前端做一些校验(rules 已经帮我们做了一些规则校验）
    // 我们主要做的是业务层面的校验
    // 校验
    const {userAccount, userPassword, checkPassword} = values;

    if (userPassword !== checkPassword) {
      message.error('用户两次输入的密码不一致');
      return;
    }

    try {
      // 注册
      const id = await register({
        ...values,
      });

      if (id > 0) {
        const defaultLoginSuccessMessage = '注册成功！';
        message.success(defaultLoginSuccessMessage, 1, () => {
          window.location.href = '/user/login';
        });
        // const urlParams = new URL(window.location.href).searchParams;
        // window.location.href = urlParams.get('redirect') || '/';
        return;
      }
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {'注册'}
          {Settings.title && ` - ${Settings.title}`}
        </title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <div className={'loginWrapper'}>
        <LoginForm
          submitter={{
            // 保留你要的文字
            searchConfig: { submitText: '注册' },
            // ProForm 官方推荐的自定义渲染方式
            render: (props) => {
              return (
                <Button
                  type="primary"
                  onClick={props.submit} // 绑定原生提交事件
                  style={{
                    backgroundColor: '#FF7D00', // 填充色
                    borderColor: '#FF7D00',     // 边框色
                    color: '#fff',              // 文字色
                    width: '328px', // 这里改成和你输入框一样的宽度
                    height: '40px',
                    fontSize: '16px',
                    borderRadius: '9px',       // 圆角（数值越大越圆润，推荐12px，适配40px高度）
                    fontFamily: 'AlibabaSans, sans-serif', // 字体（修正你原有的语法错误）
                  }}
                >
                  注册
                </Button>
              );
            },
          }}

          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={
            <div style={{
              width: '75px',    // 关键：和图片同宽
              height: '75px',   // 关键：和图片同高
              display: 'flex',
              alignItems: 'center',
              transform: 'translateX(-25px) translateY(-25px)',
            }}>
              <img
                alt="BearisBug logo"
                src="/assets/bear_bug.svg"
                style={{
                  width: '75px',
                  height: '75px',
                }}
              />
            </div>
          }
          title={
            <span style={{
              fontFamily: '"Microsoft YaHei", "PingFang SC", sans-serif',
              fontWeight: 600,         // 字体加粗
              color: '#FF8C3A',           // 字体颜色
              textShadow: '1px 1px 0 #000, -1px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000',
            }}>
                BearBug用户中心
            </span>
          }
          subTitle={
            <a href={"https://github.com/sanqian-hub"}
               target="_blank"
               style={{
                 // 完全同步title的核心样式
                 fontFamily: '"Microsoft YaHei", "PingFang SC", sans-serif',
                 fontWeight: 600,
                 color: '#A6A6A6', // 子标题改成温柔粉橘
                 fontSize: '15px', // 子标题字号稍小，区分层级
               }}

            >
              BearBug | Sanqian Hub
            </a>}

          onFinish={async (values) => {
            await handleSubmit(values as API.RegisterParams);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: (
                  <span style={{ color: '#FF8C3A', fontWeight: 'bold' }}>
                    账号密码注册
                  </span>
                ),
              },
            ]}
          />

          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined/>,
                }}
                placeholder={'请输入账号'}
                rules={[
                  {
                    required: true,
                    message: '账号是必填项！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined/>,
                }}
                placeholder={'请输入密码'}
                rules={[
                  {
                    required: true,
                    message: '密码是必填项！',
                  },
                  {
                    min: 6,
                    type: "string",
                    message: "密码长度不能小于6"
                  }
                ]}
              />
              <ProFormText.Password
                name="checkPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined/>,
                }}
                placeholder={'请再次输入密码'}
                rules={[
                  {
                    required: true,
                    message: '确认密码是必填项！',
                  },
                  {
                    min: 6,
                    type: "string",
                    message: "密码长度不能小于6"
                  }
                ]}
              />
            </>
          )}

          <div
            style={{
              marginBottom: 25,
            }}
          >

          </div>
        </LoginForm>
          </div>
      </div>
      <Footer/>
    </div>
  );
};
export default Register;
