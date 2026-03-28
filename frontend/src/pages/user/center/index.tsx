import {ProCard} from '@ant-design/pro-components';
import {Avatar, Tag, Space, Divider} from 'antd';
import {useModel} from '@umijs/max';
import dayjs from 'dayjs';
import {MailOutlined, CalendarOutlined} from '@ant-design/icons';


export default () => {
  const {initialState} = useModel('@@initialState');
  const currentUser = initialState?.currentUser;
  const maskEmail = (email: string) => {
    if (!email) return '-';
    return email.replace(/(\w{2})\w+(@.*)/, '$1***$2');
  };
  const dateFormat = (date?: Date) => {
    if (!date) return '-';
    return dayjs(date).format('YYYY-MM-DD');
  };

  return (
    <div
      style={{
        marginTop: '12%',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
      }}
    >
    <ProCard
      style={{maxWidth: 800, margin: '0 auto', padding: '20px'}}
      bordered
      boxShadow
    >

      {/* 头像 + 名字 */}
      <div style={{textAlign: 'center'}}>
        <Avatar
          size={100}
          src={currentUser?.avatarUrl}
        />
        <h2 style={{marginTop: 16}}>{currentUser?.username}</h2>
        <p style={{color: '#999'}}>热爱可抵岁月漫长~</p>
      </div>

      <Divider style={{borderTop: '1px solid rgba(255, 140, 58, 0.5)'}}></Divider>
      {/* 基本信息 */}
      <div style={{textAlign: 'center'}}>
        <div style={{marginBottom: 12, fontWeight: 500}}>基本信息</div>

        <div style={{color: '#666', display: 'flex', justifyContent: 'center', gap: 16}}>
          <span style={{display: 'flex', alignItems: 'center', gap: 6}}>
            <MailOutlined/>邮箱：
            {maskEmail(currentUser?.email || '')}
          </span>

          <span style={{color: '#ccc'}}>|</span>

          <span style={{display: 'flex', alignItems: 'center', gap: 6}}>
            <CalendarOutlined/>注册时间：
            {dateFormat(currentUser?.createTime)}
          </span>
        </div>
      </div>

      <Divider style={{borderTop: '1px solid rgba(255, 140, 58, 0.5)'}}></Divider>
    </ProCard>
    </div>
  );
};
