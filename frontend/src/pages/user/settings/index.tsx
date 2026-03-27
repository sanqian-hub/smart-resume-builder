import {ProCard, ProForm, ProFormText} from '@ant-design/pro-components';
import {Avatar, Button, message, Space, Tabs} from 'antd';
import {useModel} from '@umijs/max';
import React, {useState} from 'react';
import {updatePassword, updateUser, uploadAvatar} from "@/services/ant-design-pro/api";
import {Upload} from 'antd';
import {App} from 'antd';

/* ================= 基本信息 ================= */
const BaseInfo = React.memo(() => {
  const [uploading, setUploading] = useState(false);
  const {message} = App.useApp();

  const {initialState, setInitialState} = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  const [editing, setEditing] = useState(false);

  const maskEmail = (email: string) => {
    if (!email) return '-';
    return email.replace(/(\w{2})\w+(@.*)/, '$1***$2');
  };


  return (
    <ProCard
      style={{
        maxWidth: 900,
        margin: '0 auto',
      }}
      bordered
      boxShadow
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'stretch',
          gap: 40,
        }}
      >
        {/* 左侧 */}
        <div style={{width: 250}}>
          <div style={{textAlign: 'center'}}>
            <Avatar size={100} src={currentUser?.avatarUrl}/>

            <div style={{marginTop: 12}}>

              <Upload
                showUploadList={false} // 不显示默认列表
                beforeUpload={async (file) => {
                  setUploading(true);
                  try {
                    // 1️⃣ 调接口上传
                    const url = await uploadAvatar(file);

                    // 2️⃣ 更新全局状态 ⭐
                    setInitialState((s) => {
                      if (!s?.currentUser) return s; // ✅ 关键保护

                      return {
                        ...s,
                        currentUser: {
                          ...s.currentUser, // ✅ 一定是完整对象
                          avatarUrl: url,
                        },
                      };
                    });

                    message.success('上传成功');

                  } catch (e: any) {
                    message.error(e.message || '上传失败');
                  } finally {
                    setUploading(false);
                  }
                  return false; // ❗阻止默认上传（很关键）
                }}
              >
                <Button
                  size="small"
                  loading={uploading} // ⭐ 这一行！
                  style={{
                    borderRadius: 16,
                    border: '1px solid #ddd',
                    background: '#fafafa',
                    color: '#666',
                  }}
                >
                  修改头像
                </Button>
              </Upload>

            </div>
          </div>

          <div style={{marginTop: 24}}>
            <Space direction="vertical" style={{width: '100%'}} size={12}>
              <div style={{display: 'flex', justifyContent: 'space-between'}}>
                <span style={{color: '#999', width: 60}}>用户名</span>
                <span>{currentUser?.username}</span>
              </div>

              <div style={{display: 'flex', justifyContent: 'space-between'}}>
                <span style={{color: '#999', width: 60}}>邮箱</span>
                <span>{maskEmail(currentUser?.email || '')}</span>
              </div>
            </Space>
          </div>

          <div style={{marginTop: 24}}>
            <Button type="primary" block onClick={() => setEditing(true)}>
              修改信息
            </Button>
          </div>
        </div>

        {/* 中间虚线 */}
        <div
          style={{
            width: 1,
            borderLeft: '1px dashed rgba(255, 140, 58, 0.5)',
          }}
        />

        {/* 右侧表单 */}
        <div
          style={{
            flex: 1,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          <div style={{width: 300}}>
            <ProForm
              initialValues={{
                username: currentUser?.username,
                email: currentUser?.email,
              }}
              onFinish={async (values) => {
                try {
                  // 1. 进行用户信息更新
                  await updateUser(values);
                  message.success('修改成功');

                  // 2. 重新请求最新用户信息
                  const userInfo = await initialState?.fetchUserInfo?.();
                  if (userInfo) {
                    setInitialState((s) => ({
                      ...s,
                      currentUser: userInfo,
                    }));
                  }

                  // 3. 退出编辑状态
                  setEditing(false);
                  return true;
                } catch (error) {
                  return false;
                }
              }}
              submitter={{
                searchConfig: {
                  submitText: '保存',
                  resetText: '取消',
                },
                onReset: () => {
                  setEditing(false); // 👈 关键
                },
              }}
              layout="vertical"
            >
              <ProFormText
                name="username"
                label="用户名"
                disabled={!editing}
                rules={[{required: true}]}
              />

              <ProFormText
                name="email"
                label="邮箱"
                disabled={!editing}
                rules={[{required: true, type: 'email'}]}
              />
            </ProForm>
          </div>
        </div>
      </div>
    </ProCard>
  );
});

/* ================= 安全设置 ================= */
const Security = React.memo(() => {
  const [form] = ProForm.useForm();
  const [loading, setLoading] = useState(false);
  const { setInitialState } = useModel('@@initialState');
  return (
    <ProCard
      style={{
        maxWidth: 900,
        margin: '0 auto',
        padding: '32px 40px',
      }}
      bordered
      boxShadow
    >
      <ProForm
        form={form}
        layout="vertical"
        submitter={false} // ❗关闭默认按钮
        onFinish={async (values) => {
          setLoading(true);
          try {
            await updatePassword({
              oldPassword: values.oldPassword,
              newPassword: values.newPassword,
            });
            message.success('密码修改成功');
            form.resetFields();

            // ⏳ 延迟 1 秒执行
            setTimeout(async () => {
              // 清空全局状态
              await setInitialState((s) => ({
                ...s,
                currentUser: undefined,
              }));

              // 跳转登录页（强制刷新）
              window.location.href = '/user/login';
            }, 1000);

            return true;
          } catch (e: any) {
            // message.error(e.message || '修改失败');
            return false;
          } finally {
            setLoading(false);
          }
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'stretch', // ⭐ 关键：让左右高度一致
            gap: 60,
          }}
        >
          {/* 左侧 */}
          <div
            style={{
              width: 300,
              display: 'flex',
              alignItems: 'center',
            }}
          >
            <div style={{width: '100%'}}>  {/* ⭐ 核心这一层 */}
              <ProFormText.Password
                name="oldPassword"
                label="旧密码"
                style={{width: '100%'}}   // ⭐ 再保险
                rules={[{required: true, message: '请输入旧密码'}]}
              />
            </div>
          </div>

          {/* 中间虚线 */}
          <div
            style={{
              width: 1,
              borderLeft: '1px dashed rgba(255, 140, 58, 0.6)',
              marginTop: 22,
            }}
          />

          {/* 右侧 */}
          <div style={{width: 300}}>
            <ProFormText.Password
              name="newPassword"
              label="新密码"
              rules={[{required: true, message: '请输入新密码'}]}
            />

            <ProFormText.Password
              name="confirmPassword"
              label="确认新密码"
              dependencies={['newPassword']}
              rules={[
                {required: true, message: '请确认密码'},
                ({getFieldValue}) => ({
                  validator(_, value) {
                    if (!value || value === getFieldValue('newPassword')) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次密码不一致'));
                  },
                }),
              ]}
            />

            {/* ✅ 按钮：右侧输入框下面 */}
            <div
              style={{
                marginTop: 16,
                display: 'flex',
                justifyContent: 'flex-start',
                gap: 12,
              }}
            >
              <Button onClick={() => form.resetFields()}>
                重置
              </Button>

              <Button
                type="primary"
                loading={loading}
                onClick={() => form.submit()}
                style={{
                  background: '#ff8c3a',
                  borderColor: '#ff8c3a',
                }}
              >
                修改密码
              </Button>
            </div>
          </div>
        </div>
      </ProForm>
    </ProCard>
  );
});

/* ================= 页面 ================= */
export default () => {
  const [activeKey, setActiveKey] = useState('base');
  return (
    <div style={{ paddingTop: 120 }}>
      <div style={{ maxWidth: 900, margin: '0 auto' }}>

        {/* Tabs 只负责切换 */}
        <Tabs
          activeKey={activeKey}
          onChange={setActiveKey}
          destroyOnHidden={false}
          items={[
            { key: 'base', label: '基本信息' },
            { key: 'security', label: '安全设置' },
          ]}
        />

        {/* 内容自己控制 */}
        <div style={{ marginTop: 24 }}>
          <div style={{ display: activeKey === 'base' ? 'block' : 'none' }}>
            <BaseInfo />
          </div>

          <div style={{ display: activeKey === 'security' ? 'block' : 'none' }}>
            <Security />
          </div>
        </div>

      </div>
    </div>
  );
};
