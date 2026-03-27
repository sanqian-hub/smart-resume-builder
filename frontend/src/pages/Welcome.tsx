import { Card, Carousel } from 'antd';
import { useEffect } from 'react'; // ⭐ 一定要引入

export default () => {
  // 图片帧数
  const total = 6;
  const images = Array.from({ length: total }, (_, i) => i + 1);

  useEffect(() => {
    images.forEach((i) => {
      const img = new Image();
      img.src = `/assets/images/${i}.webp`;
    });
  }, []); // ⭐ 只执行一次
  return (
    <Card
      style={{
        maxWidth: 900,
        width: '90%',
        margin: '120px auto 0',
        borderRadius: 16,
        overflow: 'hidden',
        boxShadow: '0 10px 30px rgba(0,0,0,0.2), 0 30px 80px rgba(0,0,0,0.3)',
      }}
      styles={{
        body: {
          padding: 0, // ⭐ 替代 bodyStyle
        },
      }}
    >
      <Carousel
        autoplay
        autoplaySpeed={2500} // ⭐ 3秒切一张
        speed={400} // ⭐ 一定要加
        effect="fade"
        pauseOnHover={false} // ⭐ 关键
        dots={false}>
        {images.map((i) => (
          <div key={i}>
            <div
              style={{
                width: '100%',
                aspectRatio: '836 / 538', // ⭐ 核心
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                background: 'linear-gradient(135deg, #111, #000)',
                contain: 'layout paint', // ⭐ 就加在这里
                willChange: 'transform, opacity',
              }}
            >
              <img
                src={`/assets/images/${i}.webp`}
                loading={i === 1 ? 'eager' : 'lazy'} // ⭐
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover', // ⭐ 去白边
                  transition: 'all 0.5s ease',
                  willChange: 'transform, opacity', // ⭐ 关键
                  transform: 'translateZ(0)',
                  backfaceVisibility: 'hidden',
                }}
              />
            </div>
          </div>
        ))}
      </Carousel>
    </Card>
  );
};
