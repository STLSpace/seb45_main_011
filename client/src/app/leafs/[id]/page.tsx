'use client';

import LeafStore from '@/stores/leafStore';

import AddLeafButton from '@/components/AddLeafButton';
import Leaf from '@/components/common/Leaf';
import PageTitle from '@/components/common/PageTitle';
import Screws from '@/components/common/Screws';

export default function Leafs() {
  // URL path ID
  const { leafs } = LeafStore();
  return (
    <div className="flex justify-center items-center">
      <div className="relative w-full max-w-[720px] h-[528px] border-gradient">
        <Screws />
        <div className="pt-5 pb-4 pl-6 pr-5 flex flex-col items-center gap-5">
          <PageTitle text="내 식물 카드" />
          <div className="pr-3 w-full h-[404px] flex flex-wrap justify-center gap-4 overflow-y-scroll scrollbar">
            <AddLeafButton />
            {leafs.map((leaf) => (
              <Leaf key={leaf.leafId} location="leaf" data={leaf} />
            ))}{' '}
            {leafs.map((leaf) => (
              <Leaf key={leaf.leafId} location="leaf" data={leaf} />
            ))}{' '}
            {leafs.map((leaf) => (
              <Leaf key={leaf.leafId} location="leaf" data={leaf} />
            ))}{' '}
            {leafs.map((leaf) => (
              <Leaf key={leaf.leafId} location="leaf" data={leaf} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
