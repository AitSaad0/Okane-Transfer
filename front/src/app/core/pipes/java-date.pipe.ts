
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'javaDate', standalone: true })
export class JavaDatePipe implements PipeTransform {
  transform(value: any, format: string = 'dd/MM/yyyy HH:mm'): string | null {
    if (!value) return null;

    let date: Date;

    if (typeof value === 'string') {
      date = new Date(value);
    } else if (Array.isArray(value)) {
      const [year, month, day, hour, minute, second] = value;
      date = new Date(year, month - 1, day, hour, minute, second);
    } else {
      return null;
    }

    if (isNaN(date.getTime())) return null;

    // Format simple
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }
}
