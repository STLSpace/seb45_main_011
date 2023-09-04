export interface DefaultProps {
  className?: string;
}

export type addPrefixToHandler<T, P extends string> = {
  [K in keyof T as K extends string
    ? `${P}${K}`
    : never]: React.MouseEventHandler<HTMLButtonElement>;
};

export type LeafType = {
  id: number;
  name: string;
  date: string;
  imgUrl: string;
  diary: DiaryInfo[] | null;
};

export type PlantInfo = {
  id: number;
  name: string;
  korName: string;
  imageUrlTable: {
    sm: string;
    lg: string;
  };
  price: number;
};

export interface DiaryInfo {
  id: number;
  date: string;
  imgUrl: string;
  title: string;
  content: string;
}
type InputValues = {
  plantName: string;
  title: string;
  nickname: string;
  password: string;
  newPassword: string;
  newPasswordCheck: string;
  leafContent: string;
  diaryContent: string;
  hashTag: string;
};
