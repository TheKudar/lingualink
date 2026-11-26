import { NativeSelect } from "@/components/ui/native-select";
import { LANGUAGE_LABELS, type CourseLanguage } from "@/types/api";

interface Props {
  label: string;
  value: CourseLanguage;
  onChange: (language: CourseLanguage) => void;
  disabled: boolean;
}

export function LanguageSelect({ label, value, onChange, disabled }: Props) {
  return (
    <label className="block max-w-sm space-y-1 text-sm font-medium">
      <span>{label}</span>
      <NativeSelect value={value} disabled={disabled}
        onChange={(event) => onChange(event.target.value as CourseLanguage)}>
        {Object.entries(LANGUAGE_LABELS).map(([code, name]) =>
          <option key={code} value={code}>{name}</option>
        )}
      </NativeSelect>
    </label>
  );
}
