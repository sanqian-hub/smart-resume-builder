import {GithubOutlined} from '@ant-design/icons';
import {DefaultFooter} from '@ant-design/pro-components';
import React from 'react';

const Footer: React.FC = () => {
  return (
    <div className="footer">
      {/* ⭐ 第一行 */}
      <div className="footer-line">
        <a
          href="https://github.com/sanqian-hub"
          target="_blank"
          className="footer-github">
          <GithubOutlined style={{ marginRight: 4 }} />
          GitHub
        </a>

        <span className="divider">|</span>

        <span className="footer-copy">© Powered by Sanqian</span>
      </div>

      {/* ⭐ 第二行 */}
      <div className="footer-line footer-record">
        <a href="https://beian.miit.gov.cn/" target="_blank">
          粤ICP备2026028310号-1
        </a>

        <span className="divider">|</span>

        {/*<a href="https://beian.mps.gov.cn/#/query/webSearch?code=44088202000096"*/}
        {/*   rel="noreferrer" target="_blank">粤公网安备44088202000096号</a>*/}

        <a href="https://beian.mps.gov.cn/#/query/webSearch?code=44088202000096"
           target="_blank">
          <img
            src="/assets/beian.png"
            alt="公安网备"
            width={18}
            height={18}
          />
          <span>粤公网安备44088202000096号</span>
        </a>
      </div>
    </div>
  );
};
export default Footer;
