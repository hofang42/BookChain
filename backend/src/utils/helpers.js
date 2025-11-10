function removeAccents(str) {
  if (!str) return "";
  return str
    .normalize("NFD") // Tách chữ và dấu
    .replace(/[\u0300-\u036f]/g, "") // Xóa dấu
    .replace(/đ/g, "d") // Chuyển 'đ'
    .replace(/Đ/g, "D"); // Chuyển 'Đ'
}

module.exports = { removeAccents };
