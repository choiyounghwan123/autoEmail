import os.path
from bs4 import BeautifulSoup
import requests
from urllib.parse import urljoin
import html

base_url = "https://bce.pusan.ac.kr/bce/50189/subview.do"
url = "https://bce.pusan.ac.kr/"

TITLE_FILE = "sent_titles.txt"

def load_sent_titles():
    if os.path.exists(TITLE_FILE):
        with open(TITLE_FILE, 'r', encoding='utf-8') as f:
            return set(f.read().splitlines())
        return set()

def save_sent_title(title):
    with open(TITLE_FILE, 'a', encoding='utf-8') as f:
        f.write(title + '\n')

def crawl():
    """공지사항을 크롤링하고 텔레그램으로 전송합니다."""
    sent_titles = load_sent_titles()

    try:
        response = requests.get(base_url)
        response.raise_for_status()
    except requests.RequestException as e:
        print(f"Failed to retrieve notices page: {e}")
        return

    soup = BeautifulSoup(response.text, "html.parser")
    notices = soup.find_all('td', class_='_artclTdTitle')

    for notice in notices:
        if notice.find_parent('tr', class_="headline"):
            continue

        title = notice.find('strong').get_text(strip=True)

        # 이미 전송된 제목인지 확인
        if title in sent_titles:
            continue

        link = notice.find('a', class_="artclLinkView")['href']
        try:
            notice_response = requests.get(url + link)
            notice_response.raise_for_status()
        except requests.RequestException as e:
            print(f"Failed to retrieve notice detail page: {e}")
            continue

        notice_soup = BeautifulSoup(notice_response.text, 'html.parser')
        content = notice_soup.find('div', class_="artclView").get_text(strip=True)
        images = notice_soup.find_all("img")

        message = f"제목: {html.escape(title)}\nURL: {html.escape(url + link)}\n내용: {html.escape(content)}"

        if images:
            for image in images:
                img_url = urljoin(url, image['src'])
                img_name = os.path.basename(img_url)

                try:
                    img_response = requests.get(img_url)
                    img_response.raise_for_status()

                    os.makedirs('images', exist_ok=True)
                    img_path = os.path.join('images', img_name)

                    with open(img_path, 'wb') as f:
                        f.write(img_response.content)

                    resize_image_if_needed(img_path)

                    await send_telegram_message_with_photo(message, img_path)
                except requests.RequestException as e:
                    print(f"Failed to download or process image: {e}")
        else:
            await send_telegram_message(message)

        # 제목을 저장 파일에 기록하여 중복 전송 방지
        save_sent_title(title)