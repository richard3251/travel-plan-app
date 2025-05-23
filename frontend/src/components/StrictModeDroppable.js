import { useEffect, useState } from "react";
import { Droppable } from "@hello-pangea/dnd";

export const StrictModeDroppable = ({ children, ...props }) => {
  const [enabled, setEnabled] = useState(false);
  
  useEffect(() => {
    console.log('StrictModeDroppable 마운트됨');
    const animation = requestAnimationFrame(() => {
      console.log('StrictModeDroppable 활성화됨');
      setEnabled(true);
    });
    
    return () => {
      console.log('StrictModeDroppable 정리됨');
      cancelAnimationFrame(animation);
      setEnabled(false);
    };
  }, []);

  console.log('StrictModeDroppable 렌더링, enabled:', enabled);

  if (!enabled) {
    return <div style={{ padding: '20px', border: '1px solid red' }}>드래그 컴포넌트 로딩 중...</div>;
  }

  return <Droppable {...props}>{children}</Droppable>;
}; 