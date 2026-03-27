import type {ActionType, ProColumns} from '@ant-design/pro-components';
import {ProTable, TableDropdown} from '@ant-design/pro-components';
import {useRef} from 'react';
import {deleteUser, searchUsersByPage, updateUserByAdmin} from "@/services/ant-design-pro/api";
import {Button, Image, message, Popconfirm} from "antd";

export const waitTimePromise = async (time: number = 100) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(true);
    }, time);
  });
};

export const waitTime = async (time: number = 100) => {
  await waitTimePromise(time);
};


const columns: ProColumns<API.CurrentUser>[] = [
  {
    // dataIndex: 'id',
    valueType: 'indexBorder',
    editable: false,
    width: 48,
    render: (_, __, index, action) => {
      const currentPage = action?.pageInfo?.current || 1;
      const pageSize = action?.pageInfo?.pageSize || 5;

      const number = (currentPage - 1) * pageSize + index + 1;

      // ⭐ 每页前三
      const isTop3 = index < 3;

      return (
        <span
          style={{
            display: 'inline-block',
            minWidth: 28,
            height: 28,
            lineHeight: '28px',
            borderRadius: '50%',
            textAlign: 'center',
            background: isTop3 ? '#FF8C3A' : '#f5f5f5',
            color: isTop3 ? '#fff' : '#999',
            fontWeight: 500,
          }}
        >
      {number}
    </span>
      );
    }
  },
  {
    title: '用户名',
    dataIndex: 'username',
    copyable: true,
    tooltip: '用户名',
  },
  {
    title: '用户账户',
    dataIndex: 'userAccount',
    copyable: true,
    tooltip: '用户账号',
    editable: false,
  },
  {
    title: '头像',
    dataIndex: 'avatarUrl',
    search: false,
    copyable: true,
    tooltip: '用户头像',
    editable: false,
    render: (_, record) => {
      return <div>
        <Image src={record.avatarUrl} width={50} height={50}/>
      </div>
    }
  },
  {
    title: '邮件',
    dataIndex: 'email',
    copyable: true,
    tooltip: '邮件',
    search: false,
  },
  {
    title: '用户状态',
    dataIndex: 'userStatus',
    copyable: true,
    tooltip: '用户状态',
    valueType: 'select',
    fieldProps: {
      allowClear: true, // ⭐ 关键
    },
    valueEnum: {
      // all: { text: '超长'.repeat(50) },
      0: {
        text: '正常',
        status: 'Success',
      },
      1: {
        text: '封禁中',
        status: 'Error',
      },
    },
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    tooltip: '角色',
    valueType: 'select',
    fieldProps: {
      allowClear: true, // ⭐ 关键
    },
    valueEnum: {
      // all: { text: '超长'.repeat(50) },
      0: {
        text: '普通用户',
        status: 'Success',
      },
      1: {
        text: '管理员',
        status: 'Error',
      },
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    valueType: 'dateTime',
    tooltip: '创建时间',
    search: false,
    editable: false, // ❌ 禁止编辑
  },

  {
    title: '创建时间',
    dataIndex: 'createTimeRange',
    valueType: 'dateRange',
    search: {
      transform: (value) => ({
        startTime: value[0],
        endTime: value[1],
      }),
    },
    hideInTable: true,
  },

  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    render: (text, record, _, action) => [
      <a
        key="editable"
        onClick={() => {
          action?.startEditable?.(record.id);
        }}
      >
        编辑
      </a>,

      <span style={{ margin: '0 8px', color: '#ccc' }}>|</span>,

      <Popconfirm
        placement="topLeft" // ⭐ 关键
        key="delete"
        title="确定要删除这个用户吗？"
        onConfirm={async () => {
          const res = await deleteUser({ id: record.id });

          if (res) {
            message.success('删除成功');
            action?.reload();
          }
        }}
      >
        <a style={{ color: '#ff4d4f' }}>删除</a>
      </Popconfirm>
    ],
  }
];

export default () => {
  const actionRef = useRef<ActionType>(null);

  return (
    <ProTable<API.CurrentUser>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      request={async (params, sort, filter) => {

        // console.log(sort, filter);
        await waitTime(1000);
        const userList = await searchUsersByPage({
          ...params,
        });
        return {
          data: userList.records,
          total: userList.total,
          success: true
        }
      }}
      editable={{
        type: 'multiple',
        actionRender: (row, config, defaultDom) => {
          return [
            defaultDom.save,
            defaultDom.cancel,
          ];
        },
        onSave: async (key, record, originRow) => {
          // ⭐ 1. 计算变化字段
          const changedData: any = {};
          (Object.keys(record) as (keyof typeof record)[]).forEach((key) => {
            if (record[key] !== originRow[key]) {
              changedData[key] = record[key];
            }
          });

          // ⭐ 2. 如果没有改任何东西
          if (Object.keys(changedData).length === 0) {
            message.info('没有修改任何内容');
            return true;
          }

          // ⭐ 3. 调用接口（带 id）
          const res = await updateUserByAdmin({
            id: record.id,
            ...changedData,
          });

          if (res) {
            message.success('更新成功');
            actionRef.current?.reload(); // ⭐ 关键
            return true;
          } else {
            // message.error('更新失败');
            return false;
          }

        }
      }}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
        defaultValue: {
          option: {fixed: 'right', disable: true},
        },
        onChange(value) {
          console.log('value: ', value);
        },
      }}
      rowKey="id"
      search={{
        labelWidth: 'auto',
        optionRender: (searchConfig, formProps) => [
          <Button
            type="primary"
            key="search"
            onClick={() => formProps.form?.submit()}
          >
            查询
          </Button>,

          <Button
            key="reset"
            onClick={() => {
              formProps.form?.resetFields();

              // 2️⃣ 提交一次“空表单” → syncToUrl 会写空 URL
              formProps.form?.submit();

              // ⭐ 重新请求
              actionRef.current?.reloadAndRest?.();
            }}
          >
            重置
          </Button>,
        ],
      }}
      options={{
        setting: {
          listsHeight: 400,
        },
      }}
      // form={{
      //   // 由于配置了 transform，提交的参数与定义的不同这里需要转化一下
      //   syncToUrl: (values, type) => {
      //     if (type === 'get') {
      //       return {
      //         ...values,
      //         created_at: [values.startTime, values.endTime],
      //       };
      //     }
      //     return values;
      //   },
      // }}
      pagination={{
        defaultPageSize: 5,
        onChange: (page) => console.log(page),
        showSizeChanger: true, // 允许用户切换条数
        pageSizeOptions: ['5', '10'],
      }}
      dateFormatter="string"
      headerTitle="用户列表"
    />
  );
};
