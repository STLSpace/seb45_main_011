'use client';

import RankBoard from '@/components/board/RankBoard';

export default function Board() {
  return (
    <div className="flex flex-col justify-center items-center">
      <RankBoard />
      <div className="relative w-full max-w-[720px] h-[487px] border-gradient">
        <div className="pt-5 pb-4 pl-6 pr-5 flex flex-col gap-5"></div>
      </div>
    </div>
  );
}
